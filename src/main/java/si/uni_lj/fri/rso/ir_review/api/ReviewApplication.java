package si.uni_lj.fri.rso.ir_review.api;

import com.kumuluz.ee.discovery.annotations.RegisterService;
import com.kumuluz.ee.logs.cdi.Log;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@RegisterService
@ApplicationPath("v1")
public class ReviewApplication extends Application {
}
