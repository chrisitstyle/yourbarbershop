package pl.barbershopproject.barbershop.appointment;

import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.JpaRepository;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class AppointmentSlotRepositoryTest {

    @Test
    void shouldDeclareDeleteByVisitDateMethod() throws NoSuchMethodException {
        Method method = AppointmentSlotRepository.class.getMethod(
                "deleteByVisitDate",
                LocalDateTime.class
        );

        assertThat(method.getReturnType()).isEqualTo(void.class);
        assertThat(method.getParameterTypes()).containsExactly(LocalDateTime.class);
    }

    @Test
    void shouldExtendJpaRepositoryWithAppointmentSlotAndLongId() {
        Type repositoryInterface = AppointmentSlotRepository.class.getGenericInterfaces()[0];

        assertThat(repositoryInterface).isInstanceOf(ParameterizedType.class);

        ParameterizedType parameterizedType = (ParameterizedType) repositoryInterface;

        assertThat(parameterizedType.getRawType()).isEqualTo(JpaRepository.class);
        assertThat(parameterizedType.getActualTypeArguments())
                .containsExactly(AppointmentSlot.class, Long.class);
    }
}
