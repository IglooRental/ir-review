package si.uni_lj.fri.rso.ir_review.cdi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kumuluz.ee.discovery.annotations.DiscoverService;
import com.kumuluz.ee.logs.LogManager;
import com.kumuluz.ee.logs.Logger;
import com.kumuluz.ee.rest.beans.QueryParameters;
import com.kumuluz.ee.rest.utils.JPAUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Timeout;
import si.uni_lj.fri.rso.ir_review.models.Review;
import si.uni_lj.fri.rso.ir_review.models.dependencies.Property;
import si.uni_lj.fri.rso.ir_review.models.dependencies.User;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.List;

@RequestScoped
public class ReviewDatabase {
    private Logger log = LogManager.getLogger(ReviewDatabase.class.getName());

    @Inject
    private EntityManager em;

    // fault tolerance needs to be run through a CDI bean, so we can't just call this.method(),
    // instead, we have to inject ourselves
    @Inject
    private ReviewDatabase reviewDatabase;

    private HttpClient httpClient;
    private ObjectMapper objectMapper;

    @Inject
    @DiscoverService("property-catalogue-service")
    private String propertyCatalogueBasePath;

    @Inject
    @DiscoverService("user-service")
    private String userBasePath;

    @PostConstruct
    private void init() {
        httpClient = HttpClientBuilder.create().build();
        objectMapper = new ObjectMapper();
    }

    private User getUserObject(String json) throws IOException {
        return json == null ? new User() : objectMapper.readValue(json,
                objectMapper.getTypeFactory().constructType(User.class));
    }

    private Property getPropertyObject(String json) throws IOException {
        return json == null ? new Property() : objectMapper.readValue(json,
                objectMapper.getTypeFactory().constructType(Property.class));
    }

    public List<Review> getReviews() {
        TypedQuery<Review> query = em.createNamedQuery("Review.getAll", Review.class);
        return query.getResultList();
    }

    public List<Review> getReviewsFilter(UriInfo uriInfo) {
        QueryParameters queryParameters = QueryParameters.query(uriInfo.getRequestUri().getQuery()).defaultOffset(0).build();
        return JPAUtils.queryEntities(em, Review.class, queryParameters);
    }

    public Review getReview(String reviewId, boolean includeExtended) {
        Review review = em.find(Review.class, reviewId);
        if (review == null) {
            throw new NotFoundException();
        }
        if (includeExtended) {
            review.setUser(reviewDatabase.getUser(review.getUserId()));
            review.setProperty(reviewDatabase.getProperty(review.getPropertyId()));
        }
        return review;
    }

    public Review createReview(Review review) {
        try {
            beginTx();
            em.persist(review);
            commitTx();
        } catch (Exception e) {
            rollbackTx();
        }
        return review;
    }

    public Review putReview(String reviewId, Review review) {
        Review p = em.find(Review.class, reviewId);
        if (p == null) {
            return null;
        }
        try {
            beginTx();
            review.setId(p.getId());
            review = em.merge(review);
            commitTx();
        } catch (Exception e) {
            rollbackTx();
        }
        return review;
    }

    public boolean deleteReview(String reviewId) {
        Review p = em.find(Review.class, reviewId);
        if (p != null) {
            try {
                beginTx();
                em.remove(p);
                commitTx();
            } catch (Exception e) {
                rollbackTx();
            }
        } else {
            return false;
        }
        return true;
    }

    @CircuitBreaker(requestVolumeThreshold = 2)
    @Fallback(fallbackMethod = "getUserFallback")
    @Timeout
    public User getUser(String userId) {
        if (userBasePath != null) {
            try {
                HttpGet request = new HttpGet(userBasePath + "/v1/users/" + userId + "?includeExtended=false");
                HttpResponse response = httpClient.execute(request);

                int status = response.getStatusLine().getStatusCode();
                if (status >= 200 && status < 300) {
                    HttpEntity entity = response.getEntity();
                    if (entity != null) {
                        return getUserObject(EntityUtils.toString(entity));
                    }
                } else {
                    String msg = "Remote server '" + userBasePath + "' has responded with status " + status + ".";
                    throw new InternalServerErrorException(msg);
                }

            } catch (IOException e) {
                String msg = e.getClass().getName() + " occurred: " + e.getMessage();
                throw new InternalServerErrorException(msg);
            }
        } else {
            // service not available placeholder
            log.error("base path is null");
        }
        return new User();
    }

    public User getUserFallback(String userId) {
        User result = new User();
        result.setName("N/A");
        result.setEmail("N/A");
        return result;
    }

    @CircuitBreaker(requestVolumeThreshold = 2)
    @Fallback(fallbackMethod = "getPropertyFallback")
    @Timeout
    public Property getProperty(String propertyId) {
        if (propertyCatalogueBasePath != null) {
            try {
                HttpGet request = new HttpGet(propertyCatalogueBasePath + "/v1/properties/" + propertyId + "?includeExtended=false");
                HttpResponse response = httpClient.execute(request);

                int status = response.getStatusLine().getStatusCode();
                if (status >= 200 && status < 300) {
                    HttpEntity entity = response.getEntity();
                    if (entity != null) {
                        return getPropertyObject(EntityUtils.toString(entity));
                    }
                } else {
                    String msg = "Remote server '" + propertyCatalogueBasePath + "' has responded with status " + status + ".";
                    throw new InternalServerErrorException(msg);
                }

            } catch (IOException e) {
                String msg = e.getClass().getName() + " occurred: " + e.getMessage();
                throw new InternalServerErrorException(msg);
            }
        } else {
            // service not available placeholder
            log.error("base path is null");
        }
        return new Property();
    }

    public Property getPropertyFallback(String propertyId) {
        Property result = new Property();
        result.setLocation("N/A");
        return result;
    }

    private void beginTx() {
        if (!em.getTransaction().isActive()) {
            em.getTransaction().begin();
        }
    }

    private void commitTx() {
        if (em.getTransaction().isActive()) {
            em.getTransaction().commit();
        }
    }

    private void rollbackTx() {
        if (em.getTransaction().isActive()) {
            em.getTransaction().rollback();
        }
    }
}
