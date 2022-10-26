# 极速zip归档工具
## 开发历程
### 开发初衷
> 笔者最近在项目中遇到一个需求，将多个文件打包成zip文件供用户下载
### java自带的zip压缩工具所遇到的问题
> 经过线上试用后发现使用java自带的zip打包压缩工具很耗性能，并且速度很慢，经常会导致cpu飙升从而卡死
> 那到底是哪里损耗性能呢？ 研究发现，大部分是在压缩上消耗大量cpu性能 导致cpu飙升，并且压缩过程中耗时很长
### 解决java压缩耗时的问题
> 经过调研发现，zip打包还有一个功能，叫归并打包，顾名思义就是 只打包，不压缩,对应的java代码是 `ZipEntry.STORED`
> 我们大部分情况其实不是很关注压缩比，只是想把多个文件打包在一起，供归档使用， 
> 使用 `ZipEntry.STORED`很符合我们的需求，
> 但是在使用过程中发现，如果使用java自带的 `ZipOutputStream`进行不压缩的归并打包时需要提供每个文件的 文件大小和该文件的crc码用于放到文件头中
> 文件大小还好说，但是crc编码需要提前先把文件全读一遍用于生成crc编码，这种相当于读两遍文件，特别是网络文件的话 特别损耗性能（关于zip文档结构，大家可以自行百度）
### 解决java压缩强制需要文件大小和该文件的crc码的问题
> 经过调研发现，在zip文件结构中类型是 压缩类型 `ZipEntry.DEFLATED`的话，是会将crc编码和文件大小，单独存到zip结构的"文档描述"中而不用存到文件头中,
> （关于zip文档结构，大家可以自行百度）， 这样我们就可以先归档文件，归档完文件后我们自然就知道了文件大小和crc编码了，然后存到文档描述区域中.

### 修改java自带的 `ZipOutputStream`
> 说干就干，把java自带的 `ZipOutputStream` 拷贝出来进行改造，
> 历时一天改造完成，使用改造后的 `ZipOutputStream` 打包性能飙升，比java自带的打包**提升了不止10倍的性能**，由于不用进行压缩，cpu的损耗也降到最小
> 经过测试，使用改造后的 `ZipOutputStream` 打包出来的压缩包使用世面上的大部分压缩软件都可以打开并解压!

## 最后将改造后的代码分享出来; 
### 附一下使用方法，(和java自带的使用方法一样)
```java
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
```
