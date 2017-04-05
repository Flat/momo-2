package io.ph.bot.commands.general;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.RasterFormatException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;

import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.model.Permission;
import io.ph.util.Util;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message;

/**
 * Slice up an emoji into a 5x5 grid for Nitro purposes
 * A foray into meme territory
 * @author Paul
 *
 */
@CommandData (
		defaultSyntax = "slice",
		aliases = {"splice"},
		permission = Permission.NONE,
		description = "Slice up an Emoji into a 5x5 grid for Nitro purposes\n"
				+ "Takes in one emoji and a single, no space name after",
				example = ":thinking: nameOfEmote"
		)
public class Slice extends Command {

	@Override
	public void executeCommand(Message msg) {
		int i = Math.abs(new Random().nextInt());
		EmbedBuilder em = new EmbedBuilder();
		String name = Util.getCommandContents(Util.getCommandContents(msg));
		try {
			File saved = new File("resources/tempdownloads/" + i + "/original.png");
			saved.getParentFile().mkdirs();
			saved.createNewFile();
			new File(saved.getParentFile().getAbsolutePath() + "/splice/").mkdirs();
			Util.saveFile(new URL(msg.getEmotes().get(0).getImageUrl()), saved);
			FileUtils.copyInputStreamToFile(Waifu2x.waifu2x(saved), saved);

			BufferedImage img = ImageIO.read(saved);
			BufferedImage newImage = new BufferedImage(
					img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);

			Graphics2D g = newImage.createGraphics();
			g.drawImage(img, 0, 0, null);
			g.dispose();
			String code = cut(newImage, name, i);
			MessageBuilder mb = new MessageBuilder();
			mb.appendCodeBlock(code, "");
			msg.getChannel().sendFile(pack(new File("resources/tempdownloads/" + i + "/")), name + ".zip", 
					mb.build()).queue(success -> {
						try {
							FileUtils.deleteDirectory(saved.getParentFile());
						} catch (Exception e) {
							e.printStackTrace();
						}
					});
			
		} catch (MalformedURLException e) {
			em.setTitle("Error", null)
			.setColor(Color.RED)
			.setDescription("Error finding a valid Emoji to slice");
			msg.getChannel().sendMessage(em.build()).queue();
		} catch (IOException e) {
			e.printStackTrace();
			em.setTitle("Error", null)
			.setColor(Color.RED)
			.setDescription("Error processing the image");
			msg.getChannel().sendMessage(em.build()).queue();
		}
	}

	/**
	 * Cut an image into a 5x5 grid
	 * @param img Image to cut
	 * @param nameOfEmoji Name of the emoji
	 * @param directoryNum Number where the temp resources are stored
	 * @return String of array to use
	 * @throws IOException Error accessing files
	 */
	private static String cut(BufferedImage img, String nameOfEmoji, int directoryNum) throws IOException {
		BufferedImage[][] array;
		img = scale(img, 320, 320);
		int height = img.getHeight();
		int width = img.getWidth();
		// How big each slice is
		int columnSlice = 64;
		int rowSlice = 64;

		int columns = height / columnSlice;
		int rows = width / rowSlice;

		array = new BufferedImage[columns][rows];
		int counter = 0;
		StringBuilder copy = new StringBuilder();
		for (int j = 0; j < rows; j++) {
			for (int i = 0; i < columns; i++) {
				try {
					array[i][j] = img.getSubimage(i * columnSlice, j * rowSlice, columnSlice, rowSlice);
				} catch (RasterFormatException e) {
					try {
						if (e.getMessage().contains("y + height")) {
							array[i][j] = img.getSubimage(i * columnSlice, j * rowSlice, columnSlice, height - (j * rowSlice));
						} else if (e.getMessage().contains("x + height")) {
							array[i][j] = img.getSubimage(i * columnSlice, j * rowSlice, columnSlice - (i * columnSlice), height);
						}
					} catch (RasterFormatException e1) {
						continue;
					}
				}
				copy.append(String.format(":%s%d:", nameOfEmoji, counter));
				ImageIO.write(array[i][j], "png", new File("resources/tempdownloads/" 
						+ directoryNum + "/splice/" + nameOfEmoji + counter++ + ".png"));

			}
			copy.append("\n");
		}

		return copy.toString();
	}

	/**
	 * Scale an image
	 * @param imageToScale Image to scale
	 * @param newWidth New width
	 * @param newHeight New height
	 * @return Bufferedimage scaled w/ an alpha channel
	 */
	private static BufferedImage scale(BufferedImage imageToScale, int newWidth, int newHeight) {
		BufferedImage scaledImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics2D = scaledImage.createGraphics();
		graphics2D.drawImage(imageToScale, 0, 0, newWidth, newHeight, null);
		graphics2D.dispose();
		return scaledImage;
	}

	/**
	 * Pack emojis into one zip file
	 * @param directory Directory that includes the folder (which is stored in /splice/*.png)
	 * @return Zipped file
	 * @throws IOException 
	 */
	private static File pack(File directory) throws IOException {
		byte[] buffer = new byte[1024];
		File toReturn = new File(directory.getAbsolutePath() + "/splice.zip");
		ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(toReturn));

		File dir = new File(directory.getAbsolutePath() + "/splice/");
		for (File f : dir.listFiles()) {
			FileInputStream in = new FileInputStream(f);
			
			zip.putNextEntry(new ZipEntry(f.getName()));
			int length;

			while((length = in.read(buffer)) > 0) {
				zip.write(buffer, 0, length);
			}
			in.close();
			zip.closeEntry();
		}
		zip.close();

		return toReturn;
	}
}
