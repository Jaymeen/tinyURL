package org.infinitesolutions.tinyurl.common.util;

import org.springframework.stereotype.Service;

@Service
public class Base62Encoder {
    private static final char[] BASE62 = new char[] {
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'
    };

    public static String encode(long value) {
        StringBuilder result = new StringBuilder();

        while(value > 0) {
            int index = (int) (value % BASE62.length);
            result.append(BASE62[index]);
            value /= BASE62.length;
        }

        return result.reverse().toString();
    }
}
