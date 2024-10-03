package pro.cyrent.anticheat.api.check;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface CheckInformation {
    boolean enabled() default true;
    boolean experimental() default false;
    boolean punishable() default true;
    double punishmentVL() default 20.0;

    String name();
    String subName() default "A";
    String description();

    CheckState state();

    CheckName checkNameEnum();

    CheckType checkType() default CheckType.MOVEMENT;
}
