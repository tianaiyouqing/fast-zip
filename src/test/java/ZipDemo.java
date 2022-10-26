import cloud.tianai.zip.ZipEntry;
import cloud.tianai.zip.ZipOutputStream;

import java.io.*;

public class ZipDemo {


    /**
     * 将 aaa.pdf 和 bbb.zip 进行归并打包
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        FileOutputStream outputStream = new FileOutputStream("C:\\Users\\Thinkpad\\Desktop\\aaa123.zip");

        ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);
        zipOutputStream.putNextEntry(new ZipEntry("aaa.pdf"));
        FileInputStream fileInputStream = new FileInputStream("C:\\Users\\Thinkpad\\Desktop\\aaa.pdf");
        copy(fileInputStream, zipOutputStream);
        zipOutputStream.closeEntry();

        zipOutputStream.putNextEntry(new ZipEntry("bbb.zip"));
        fileInputStream = new FileInputStream("C:\\Users\\Thinkpad\\Desktop\\bbb.zip");
        copy(fileInputStream, zipOutputStream);
        zipOutputStream.closeEntry();

        zipOutputStream.finish();
        zipOutputStream.close();
    }

    // 拷贝 apache 的 拷贝方法

    public static int copy(final InputStream input, final OutputStream output) throws IOException {
        final long count = copy(input, output, 1024 * 4);
        if (count > Integer.MAX_VALUE) {
            return -1;
        }
        return (int) count;
    }

    public static long copy(final InputStream input, final OutputStream output, final int bufferSize)
            throws IOException {
        return copyLarge(input, output, new byte[bufferSize]);
    }

    public static long copyLarge(final InputStream input, final OutputStream output, final byte[] buffer)
            throws IOException {
        long count = 0;
        int n;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }
}
