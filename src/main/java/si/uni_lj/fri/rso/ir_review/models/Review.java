package si.uni_lj.fri.rso.ir_review.models;

import org.eclipse.persistence.annotations.UuidGenerator;
import si.uni_lj.fri.rso.ir_review.models.dependencies.Property;
import si.uni_lj.fri.rso.ir_review.models.dependencies.User;

import javax.persistence.*;

@Entity(name = "reviews")
@NamedQueries(value = {
        @NamedQuery(name = "Review.getAll", query = "SELECT u FROM reviews u")
})
@UuidGenerator(name = "idGenerator")
public class Review {
    @Id
    @GeneratedValue(generator = "idGenerator")
    private String id;

    private String header;

    private String message;

    private Integer score;

    @Column(name = "user_id")
    private String userId;

    @Transient
    private User user;

    @Column(name = "property_id")
    private String propertyId;

    @Transient
    private Property property;

    public Review() {}

    public Review(String id, String header, String message, Integer score, String userId, String propertyId) {
        this.id = id;
        this.header = header;
        this.message = message;
        this.score = score;
        this.userId = userId;
        this.propertyId = propertyId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(String propertyId) {
        this.propertyId = propertyId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Property getProperty() {
        return property;
    }

    public void setProperty(Property property) {
        this.property = property;
    }
}
