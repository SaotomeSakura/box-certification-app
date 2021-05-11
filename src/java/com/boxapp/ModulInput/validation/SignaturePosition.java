package src.java.com.boxapp.ModulInput.validation;

import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.InMemoryDocument;
import eu.europa.esig.dss.pades.DSSFont;
import eu.europa.esig.dss.pades.SignatureImageParameters;
import eu.europa.esig.dss.pades.SignatureImageTextParameters;
import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import src.java.com.boxapp.ModulInput.signature.SignatureCreator;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Data holder class for signature position for a PAdES visible signature.
 * The coordinates and size are relative and must be in range 0.0 - 1.0.
 * The absolute coordinates depend on the page size and are calculated just before the signing.
 *
 */
public class SignaturePosition {
    private int pageIndex;

    public int getPageIndex() {
        return pageIndex;
    }

    private double x;

    public double getX() {
        return x;
    }

    private double y;

    public double getY() {
        return y;
    }

    private double width;

    public double getWidth() {
        return width;
    }

    private double height;

    public double getHeight() {
        return height;
    }

    public SignaturePosition(int pageIndex, double x, double y, double width, double height) {
        if (pageIndex < 0) throw new IllegalArgumentException();
        if (x < 0 || x > 1) throw new IllegalArgumentException();
        if (y < 0 || y > 1) throw new IllegalArgumentException();
        if (width < 0 || width > 1) throw new IllegalArgumentException();
        if (height < 0 || height > 1) throw new IllegalArgumentException();

        this.pageIndex = pageIndex;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /**
     * Calculates PAdES SignatureImageParameters necessary for a visible signature using the given parameters.
     *
     * @param pdfDocumentPath
     * @param signatureText
     * @param signaturePosition
     * @return
     * @throws IOException
     */
    public static SignatureImageParameters getSignatureImageParameters(String pdfDocumentPath, String signatureText, SignaturePosition signaturePosition) throws IOException {
        if (signaturePosition == null) throw new NullPointerException("signaturePosition");

        // Set signature text.
        SignatureImageTextParameters textParameters = new SignatureImageTextParameters();
        textParameters.setText(signatureText);
        final int fontSize = 11;
        textParameters.setFont((DSSFont) new Font(null, Font.PLAIN, fontSize));
        textParameters.setTextColor(Color.BLACK);
        //textParameters.setSignerTextHorizontalAlignment(...);
        //textParameters.setSignerNamePosition(...);
        SignatureImageParameters imageParameters = new SignatureImageParameters();
        imageParameters.setTextParameters(textParameters);

        // Set signature image.
        InputStream imageStream = SignatureCreator.class.getResourceAsStream("/icons/pen-64.png");
        try {
            DSSDocument imageDocument = new InMemoryDocument(imageStream);
            imageParameters.setImage(imageDocument);
            //imageParameters.setSignerTextImageVerticalAlignment(...);
        } finally {
            IOUtils.closeQuietly(imageStream);
        }

        // Get the page count and page dimensions of the last page.
        PDDocument pdDocument = PDDocument.load(new File(pdfDocumentPath));
        PDRectangle pageArea = pdDocument.getPage(signaturePosition.getPageIndex()).getTrimBox(); // https://www.prepressure.com/pdf/basics/page-boxes
        imageParameters.setPage(signaturePosition.getPageIndex() + 1); // Set the page as the last page (counted from 1).
        pdDocument.close();

        // Calculate the maximal signature area (maximum width and height possible).
        final Dimension maximalSignatureArea = new Dimension((int)(pageArea.getWidth() * signaturePosition.getWidth()),
                (int)(pageArea.getHeight() * signaturePosition.getHeight()));

        // Set the width and height of the signature.
        // Try to use optimal parameters if they fit into the maxSignatureArea.
        final Dimension optimalSignatureArea = getOptimalSignatureArea(imageParameters);

        if (optimalSignatureArea.width > maximalSignatureArea.width)
            imageParameters.setWidth(maximalSignatureArea.width);
        else
            imageParameters.setWidth(optimalSignatureArea.width);

        if (optimalSignatureArea.height > maximalSignatureArea.height)
            imageParameters.setHeight(maximalSignatureArea.height);
        else
            imageParameters.setHeight(optimalSignatureArea.height);

        // Set the X and Y position of the signature using the width, height, row and column.
        imageParameters.setxAxis((float) (pageArea.getWidth() * signaturePosition.getX()));
        imageParameters.setyAxis((float) (pageArea.getHeight() * signaturePosition.getY()));

        return imageParameters;
    }

    /**
     * This method returns the image size with the original parameters (the generation uses DPI)
     *
     * @param imageParameters
     *            the image parameters
     * @return a Dimension object
     * @throws IOException
     */
    private static Dimension getOptimalSignatureArea(SignatureImageParameters imageParameters) throws IOException {
        int width = 0;
        int height = 0;

        DSSDocument docImage = imageParameters.getImage();
        if (docImage != null) {
            BufferedImage image = ImageIO.read(docImage.openStream());
            width = image.getWidth();
            height = image.getHeight();
        }

        SignatureImageTextParameters textParamaters = imageParameters.getTextParameters();
        return null;
    }


    private static int getDpi(Integer dpi) {
        int result = 300; // Default value.
        if (dpi != null && dpi.intValue() > 0) {
            result = dpi.intValue();
        }
        return result;
    }

}
