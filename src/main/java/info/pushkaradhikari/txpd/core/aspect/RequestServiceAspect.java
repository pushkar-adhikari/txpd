package info.pushkaradhikari.txpd.core.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class RequestServiceAspect {

	@Around("execution(public * (@info.pushkaradhikari.txpd.core.business.annotation.TXPDService *).*(..))")
	public Object around(ProceedingJoinPoint point) throws Throwable {
		try {
			return point.proceed();
		} catch (Exception e) {
			throw e;
		}
	}
}
