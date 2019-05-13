package com.guosen.webx.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import sun.awt.image.ToolkitImage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 参考 http://blog.csdn.net/lcx46/article/details/13626841
 *
 * @author 曹飞龙
 */
public class QrImageUtils {
    private final static int FRAME_WIDTH = 1;

    // 二维码写码器
    private static MultiFormatWriter mutiWriter = new MultiFormatWriter();

    /**
     * 生成带有中间小图标的二维码图片
     *
     * @param content    二维码内容
     * @param width      二维码宽度
     * @param height     二维码高度
     * @param srcImageIs 小图标的流
     * @param os         输出二维码的流
     * @param imageW     小图标的宽度
     * @param imageH     小图标的高度
     * @throws IOException
     * @throws WriterException
     */
    public static void encode(String content, int width, int height, InputStream srcImageIs, OutputStream os,
                              int imageW, int imageH) throws IOException, WriterException {
        ImageIO.write(genBarcode(content, width, height, srcImageIs, imageW, imageH), "jpg", os);
    }

    private static BufferedImage genBarcode(String content, int width, int height, InputStream srcImageIs, int imageW,
                                            int imageH) throws WriterException, IOException {
        return genBarcode(content, width, height, srcImageIs, imageW, imageH, 0xff000000);
    }

    private static BufferedImage genBarcode(String content, int width, int height, InputStream srcImageIs, int imageW,
                                            int imageH, int colorFront) throws WriterException, IOException {
        int imageWidthHalf = imageW / 2;
        BufferedImage scaleImage = scale(srcImageIs, imageW, imageH, false);

        int[][] srcPixels = new int[imageW][imageH];
        for (int i = 0; i < scaleImage.getWidth(); i++) {
            for (int j = 0; j < scaleImage.getHeight(); j++) {
                srcPixels[i][j] = scaleImage.getRGB(i, j);
            }
        }

        Map<EncodeHintType, Object> hint = new HashMap<EncodeHintType, Object>();
        hint.put(EncodeHintType.CHARACTER_SET, "utf-8");
        hint.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hint.put(EncodeHintType.MARGIN, 1);

        // 生成二维码
        BitMatrix matrix = mutiWriter.encode(content, BarcodeFormat.QR_CODE, width, height, hint);

        // 二维矩阵转为一维像素数组
        int halfW = matrix.getWidth() / 2;
        int halfH = matrix.getHeight() / 2;
        int[] pixels = new int[width * height];

        // System.out.println(matrix.getHeight());
        for (int y = 0; y < matrix.getHeight(); y++) {
            for (int x = 0; x < matrix.getWidth(); x++) {
                // 读取图片
                if (x > halfW - imageWidthHalf && x < halfW + imageWidthHalf && y > halfH - imageWidthHalf
                        && y < halfH + imageWidthHalf) {
                    pixels[y * width + x] = srcPixels[x - halfW + imageWidthHalf][y - halfH + imageWidthHalf];
                }
                // 在图片四周形成边框
                else if ((x > halfW - imageWidthHalf - FRAME_WIDTH && x < halfW - imageWidthHalf + FRAME_WIDTH
                        && y > halfH - imageWidthHalf - FRAME_WIDTH && y < halfH + imageWidthHalf + FRAME_WIDTH)
                        || (x > halfW + imageWidthHalf - FRAME_WIDTH && x < halfW + imageWidthHalf + FRAME_WIDTH
                        && y > halfH - imageWidthHalf - FRAME_WIDTH && y < halfH + imageWidthHalf + FRAME_WIDTH)
                        || (x > halfW - imageWidthHalf - FRAME_WIDTH && x < halfW + imageWidthHalf + FRAME_WIDTH
                        && y > halfH - imageWidthHalf - FRAME_WIDTH && y < halfH - imageWidthHalf + FRAME_WIDTH)
                        || (x > halfW - imageWidthHalf - FRAME_WIDTH && x < halfW + imageWidthHalf + FRAME_WIDTH
                        && y > halfH + imageWidthHalf - FRAME_WIDTH
                        && y < halfH + imageWidthHalf + FRAME_WIDTH)) {
                    pixels[y * width + x] = 0xfffffff;
                } else {
                    // 此处可以修改二维码的颜色，可以分别制定二维码和背景的颜色；
                    pixels[y * width + x] = matrix.get(x, y) ? colorFront : 0xfffffff;
                }
            }
        }

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        image.getRaster().setDataElements(0, 0, width, height, pixels);

        return image;
    }

    /**
     * 把传入的原始图像按高度和宽度进行缩放，生成符合要求的图标
     *
     * @param srcImageIs 源文件地址
     * @param height     目标高度
     * @param width      目标宽度
     * @param hasFiller  比例不对时是否需要补白：true为补白; false为不补白;
     * @throws IOException
     */
    private static BufferedImage scale(InputStream srcImageIs, int height, int width, boolean hasFiller)
            throws IOException {
        double ratio = 0.0; // 缩放比例

        BufferedImage srcImage = ImageIO.read(srcImageIs);
        Image destImage = srcImage.getScaledInstance(width, height, BufferedImage.SCALE_SMOOTH);
        // 计算比例
        if ((srcImage.getHeight() > height) || (srcImage.getWidth() > width)) {
            if (srcImage.getHeight() > srcImage.getWidth()) {
                ratio = (new Integer(height)).doubleValue() / srcImage.getHeight();
            } else {
                ratio = (new Integer(width)).doubleValue() / srcImage.getWidth();
            }
            AffineTransformOp op = new AffineTransformOp(AffineTransform.getScaleInstance(ratio, ratio), null);
            destImage = op.filter(srcImage, null);
        } else {
            ratio = (new Integer(height)).doubleValue() / srcImage.getHeight();
            AffineTransformOp op = new AffineTransformOp(AffineTransform.getScaleInstance(ratio, ratio), null);
            destImage = op.filter(srcImage, null);
        }
        if (hasFiller) {
            // 补白
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphic = image.createGraphics();
            graphic.setColor(Color.white);
            graphic.fillRect(0, 0, width, height);
            if (width == destImage.getWidth(null))
                graphic.drawImage(destImage, 0, (height - destImage.getHeight(null)) / 2, destImage.getWidth(null),
                        destImage.getHeight(null), Color.white, null);
            else
                graphic.drawImage(destImage, (width - destImage.getWidth(null)) / 2, 0, destImage.getWidth(null),
                        destImage.getHeight(null), Color.white, null);
            graphic.dispose();
            destImage = image;
        }

        if(destImage instanceof  BufferedImage) {
            return (BufferedImage)destImage;
        }
        ToolkitImage tmp = (ToolkitImage) destImage;
        return tmp.getBufferedImage();
    }
}
