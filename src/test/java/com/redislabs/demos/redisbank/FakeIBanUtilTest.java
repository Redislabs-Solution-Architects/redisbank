package com.redislabs.demos.redisbank;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class FakeIBanUtilTest {

    @Test
    void sameIbanForSameUserName() {
        String iban1 = Utilities.generateFakeIbanFrom("lars");
        String iban2 = Utilities.generateFakeIbanFrom("lars");
        assertEquals(iban2, iban1, "Generated fake IBANs should be equal for identical usernames");
    }

    @Test
    void differentIbanForDifferentUserName() {
        String iban1 = Utilities.generateFakeIbanFrom("lars");
        String iban2 = Utilities.generateFakeIbanFrom("dashaun");
        assertNotEquals(iban2, iban1, "Generated fake IBANs should not be equal for different usernames");
    }

    @Test
    void nullUserName()    {
        assertThrows(IllegalArgumentException.class, () ->  {
            Utilities.generateFakeIbanFrom(null);
        });
    }

    @Test
    void blankUserName()    {
        assertThrows(IllegalArgumentException.class, () ->  {
            Utilities.generateFakeIbanFrom("");
        });
    }

}
