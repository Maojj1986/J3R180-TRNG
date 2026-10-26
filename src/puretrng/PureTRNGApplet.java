package puretrng;

import javacard.framework.*;
import javacard.security.RandomData;

public class PureTRNGApplet extends Applet {

    private RandomData trng;

    protected PureTRNGApplet() {
        // ALG_SECURE_RANDOM (常量 2) 在 NXP J3R180 底层直接调用物理硬件真随机数发生器 (TRNG)
        trng = RandomData.getInstance(RandomData.ALG_SECURE_RANDOM);
        register();
    }

    public static void install(byte[] bArray, short bOffset, byte bLength) {
        new PureTRNGApplet();
    }

    public void process(APDU apdu) {
        if (selectingApplet()) {
            return;
        }

        byte[] buffer = apdu.getBuffer();

        // 只响应 00 84 00 00 20 指令
        if (buffer[ISO7816.OFFSET_INS] != (byte) 0x84) {
            ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
        }

        short le = apdu.setOutgoing();
        if (le <= 0 || le > 256) {
            le = 32;
        }

        // 硬件 TRNG 直接填充 32 字节真随机数
        trng.generateData(buffer, (short) 0, le);

        apdu.setOutgoingLength(le);
        apdu.sendBytes((short) 0, le);
    }
}
