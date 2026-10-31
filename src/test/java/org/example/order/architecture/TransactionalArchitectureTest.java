package org.example.order.architecture;

import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaMethodCall;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import org.springframework.transaction.annotation.Transactional;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

@AnalyzeClasses(packages = "org.example.order")
class TransactionalArchitectureTest {

    @ArchTest
    static final ArchRule transactional_methods_should_not_be_called_via_self_invocation =
            methods()
                    .that().areAnnotatedWith(Transactional.class)
                    .should(new ArchCondition<JavaMethod>("only be called through the Spring proxy, never via self-invocation") {
                        @Override
                        public void check(JavaMethod method, ConditionEvents events) {
                            for (JavaMethodCall call : method.getCallsOfSelf()) {
                                if (call.getOriginOwner().equals(method.getOwner())) {
                                    String message = String.format(
                                            "%s is annotated with @Transactional but is called from %s within the same class (%s) - "
                                                    + "this bypasses the Spring AOP proxy, so the transaction semantics silently do not apply",
                                            method.getFullName(), call.getOrigin().getFullName(), method.getOwner().getName());
                                    events.add(SimpleConditionEvent.violated(call, message));
                                }
                            }
                        }
                    });
}
