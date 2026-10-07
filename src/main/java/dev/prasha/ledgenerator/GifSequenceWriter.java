package dev.prasha.ledgenerator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.RenderedImage;
import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;

final class GifSequenceWriter implements Closeable {
    private final ImageWriter writer;
    private final ImageWriteParam parameters;
    private final IIOMetadata metadata;

    GifSequenceWriter(Path target, int imageType, int delayMillis, boolean loop) throws IOException {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersBySuffix("gif");
        if (!writers.hasNext()) {
            throw new IOException("No GIF writer available");
        }

        writer = writers.next();
        parameters = writer.getDefaultWriteParam();
        ImageTypeSpecifier imageTypeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(imageType);
        metadata = writer.getDefaultImageMetadata(imageTypeSpecifier, parameters);
        configureMetadata(metadata, delayMillis, loop);

        ImageOutputStream outputStream = new FileImageOutputStream(target.toFile());
        writer.setOutput(outputStream);
        writer.prepareWriteSequence(null);
    }

    void writeFrame(RenderedImage image) throws IOException {
        writer.writeToSequence(new IIOImage(image, null, metadata), parameters);
    }

    @Override
    public void close() throws IOException {
        try {
            writer.endWriteSequence();
        } finally {
            Object output = writer.getOutput();
            writer.dispose();
            if (output instanceof ImageOutputStream imageOutputStream) {
                imageOutputStream.close();
            }
        }
    }

    private void configureMetadata(IIOMetadata gifMetadata, int delayMillis, boolean loop) throws IOException {
        String formatName = gifMetadata.getNativeMetadataFormatName();
        IIOMetadataNode root = (IIOMetadataNode) gifMetadata.getAsTree(formatName);

        IIOMetadataNode graphicsControlExtension = getNode(root, "GraphicControlExtension");
        graphicsControlExtension.setAttribute("disposalMethod", "none");
        graphicsControlExtension.setAttribute("userInputFlag", "FALSE");
        graphicsControlExtension.setAttribute("transparentColorFlag", "FALSE");
        graphicsControlExtension.setAttribute("delayTime", Integer.toString(Math.max(1, delayMillis / 10)));
        graphicsControlExtension.setAttribute("transparentColorIndex", "0");

        IIOMetadataNode applicationExtensions = getNode(root, "ApplicationExtensions");
        IIOMetadataNode applicationNode = new IIOMetadataNode("ApplicationExtension");
        applicationNode.setAttribute("applicationID", "NETSCAPE");
        applicationNode.setAttribute("authenticationCode", "2.0");
        applicationNode.setUserObject(new byte[]{0x1, (byte) (loop ? 0 : 1), 0});
        applicationExtensions.appendChild(applicationNode);

        gifMetadata.setFromTree(formatName, root);
    }

    private IIOMetadataNode getNode(IIOMetadataNode rootNode, String nodeName) {
        for (int index = 0; index < rootNode.getLength(); index++) {
            if (rootNode.item(index).getNodeName().equalsIgnoreCase(nodeName)) {
                return (IIOMetadataNode) rootNode.item(index);
            }
        }

        IIOMetadataNode node = new IIOMetadataNode(nodeName);
        rootNode.appendChild(node);
        return node;
    }
}