package csci310group11.Implementation;


import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.imageio.ImageIO;

public class CollageGenerator {

	private ArrayList<BufferedImage> images; //change to <BufferedImage> if necessary
	private ArrayList<BufferedImage> borderedImages;
	private Collage collage;
	private BufferedImage collageImage;
	private GoogleCustomSearchApi api;

	public CollageGenerator() {
		this.images = new ArrayList<BufferedImage>();
		this.borderedImages = new ArrayList<BufferedImage>();
		this.collage = new Collage();
		this.api = new GoogleCustomSearchApi();
		collageImage = new BufferedImage(1000, 750, BufferedImage.TYPE_INT_ARGB);
	}

	/**
	 * Driver method to complete Collage creation process.
	 * This method will call the GoogleCustomSearchAPI and retrieve the images to be compiled.
	 * Each image will be resized, then given a border.
	 * Each image will be complied into one BufferedImage stored in a Collage Object.
	 * 
	 * The Collage Object will be downloaded to the Server storage space and the url will be returned to the Servlet
	 * to be sent back out to the frontend.
	 * 
	 * @param topic the String of terms inputted by the user; this will be passed to the API to make th search
	 * @return URL of the collage on the server's storage system
	 */
	public String collageGeneratorDriver(String topic) {

		try {
			this.images = (ArrayList<BufferedImage>) this.api.execute(topic);
			// List<BufferedImage> t = this.api.execute(topic);
			// this.images = (ArrayList<BufferedImage>) t;	
		} catch (InsufficientImagesFoundError iife) {
			System.out.println("iife: " + iife.getMessage());
			return null;
		}

		this.resizeImages();
		this.addBorderToImages();
		this.compileCollage();
		
		return this.downloadCollage(this.collage);
	}

	/**
	 * Getter for the collage image
	 * 
	 * @return the BufferedImage collage
	 */
	public BufferedImage getCollage() {
		return this.collage.getCollageImage();
	}

	/**
	 * Resizes all the BufferedImages inside of images to be 1/20th of the size of the overall collage.
	 * 
	 * Takes the original image and draws it into a new BufferedImage with the proper dimensions
	 * 
	 * Notes:
	 * Should we set collage size 1st or make collage based of images entered?
	 */
	private void resizeImages() {
		//1/20th of collage dimensions
		//BufferedImage collageImage = this.collage.getCollageImage();
		int resizeWidth = this.collageImage.getWidth()/5;
		int resizeHeight = this.collageImage.getHeight()/4;
		
		//Iterate through all images
		for(int i=0; i < images.size(); i++) {
			BufferedImage img = images.get(i);
			//New BufferedImage with 1/20th dimensions of collage
			BufferedImage resizeImg = new BufferedImage(resizeWidth, resizeHeight, img.getType());

			//Draws the img image into the size of the resizeImg
			Graphics2D graphics = resizeImg.createGraphics();
			graphics.drawImage(img, 0, 0, resizeWidth, resizeHeight, null);

			//replace BufferedImage in images with resizedImg
			images.set(i, resizeImg);
			graphics.dispose(); //not sure if needed
		}
	}
	
	/**
	 * Responsible for adding a 3px white border to each image to be added to the collage.
	 * 
	 * Creates a new BufferedImage that is 6px taller and wider than the original BufferedImage in images.
	 * 
	 * Sets thes the graphics of the larger BufferedImage to white. Paints the original image onto the 
	 * new BufferedImage to create a 3px "border". Adds the bordred BuffereImage to this.borderedImages.
	 */
	private void addBorderToImages() {
		for(int i=0; i < images.size(); i++) {
			BufferedImage image = images.get(i);
			int width = image.getWidth();
			int height = image.getHeight();
			
			//Create image with enough space for 3px border
			BufferedImage borderedImage = new BufferedImage(width + 2*Constants.BORDER_WIDTH, height + 2*Constants.BORDER_WIDTH, image.getType());

			//Setting larger image to all white
			Graphics2D graphics = borderedImage.createGraphics();
			graphics.setPaint(Color.WHITE);
			graphics.fillRect(0, 0, borderedImage.getWidth(), borderedImage.getHeight());

			//Paint original image onto new borderedImage	
			graphics.drawImage(image, Constants.BORDER_WIDTH, Constants.BORDER_WIDTH, null);
			this.borderedImages.add(borderedImage);	
			graphics.dispose(); // not sure if needed check with both
		}
	}

	/*
	 * Cover corners, lay inside and see what happens
	 * 
	 * Rotate inside of collage to allow for chopping of the corners.
	 * Record location inside of collage
	 */
	private void compileCollage() {
		Graphics2D graphics = this.collageImage.createGraphics();
		graphics.setPaint(Color.WHITE); //check for "whitespace"
		graphics.fillRect(0, 0, this.collageImage.getWidth(), this.collageImage.getHeight());

		for(int r=0; r < 5; r++) { //rows of images
			for(int c = 0; c < 6; c++) { //cols of images
				BufferedImage currImage = borderedImages.get(5*r + c);
				System.out.println("Drawing image: " + (5*r + c));
				int row = this.collageImage.getHeight()/5 * r;
				if(r == 0) {
					row -= 25;
				}
				if(r == 1) {
					row -= 15;
				}
				if(r == 4) {
					row += 2;
				}
				
				int col = this.collageImage.getWidth()/6 * c;
				if(c == 0) {
					col -= 20;
				}
				if(c == 5) {
					col += 10;
				}

				this.rotateAndDrawImage(currImage, row, col);
			}
		}
		
		// for(int r=0; r < 3; r++) {
		// 	for(int c=0; c < 3; c++) {
		// 		BufferedImage currImage = borderedImages.get(3*r + c + 17);
		// 		int row = this.collageImage.getHeight()/3 * r + this.collageImage.getHeight()/8;
		// 		int col =  this.collageImage.getWidth()/3 * c + this.collageImage.getWidth()/8;

		// 		this.rotateAndDrawImage(currImage, row, col);
		// 	}
		// }
		
		// this.rotateAndDrawImage(this.borderedImages.get(29), 350, 250);
		
	}

	/**
	 * Helper method to rotate images. Will draw them onto the collage BufferedImage
	 */
	private void rotateAndDrawImage(BufferedImage image, int row, int col) {
		AffineTransform at = new AffineTransform();

		at.translate(col, row); //translate onto position for collage

		int degree = (int) (Math.random() * 91 - 45); //-45 to 45
		at.rotate(Math.toRadians(degree), image.getWidth()/2, image.getHeight()/2);

		AffineTransformOp op = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
		op.filter(image, this.collageImage); //paints onto collageImage
	}
	
	private String downloadCollage(Collage collage) {
		String filename = "";
		BufferedImage image = collage.getCollageImage();
	
		try {
			//get destination path in assets folder of server
			File assetsDirectory = new File(System.getProperty("user.dir") + "/assets");
			assetsDirectory.mkdir(); //no exception if directory already exists
			
			filename += System.getProperty("user.dir") + "/assets/"; //current system context path
			filename += "topicName";
			filename += System.currentTimeMillis() + ".png";    
			File outputFile = new File(filename);
			ImageIO.write(image, "png", outputFile);
			
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		return filename;
	}

	
	public void setImages(ArrayList<BufferedImage> images) {
		this.images = images;
	}

	public void dummyDriver() {

		this.resizeImages();
		this.addBorderToImages();
		this.compileCollage();
		
		try {
			File outFile = new File("collage.png");
			ImageIO.write(this.collageImage, "png", outFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public static void main(String[] args) throws MalformedURLException {
		CollageGenerator cg = new CollageGenerator();
		try {
			URL url = new URL("https://media.wired.com/photos/5a7cab6ca8e48854db175890/master/pass/norwayskier-915599900.jpg");
			BufferedImage image = ImageIO.read(url);
			
			URL url2 = new URL("https://pbs.twimg.com/profile_images/953320896101412864/UdE5mfkP_400x400.jpg");
			BufferedImage image2 = ImageIO.read(url2);

			ArrayList<BufferedImage> images = new ArrayList<BufferedImage>();
			for(int i=0; i < 30; i++) {
				if(i % 2 == 0) {
					images.add(image);
				} else {
					images.add(image2);
				}
			}

			cg.setImages(images);
			cg.dummyDriver();
			

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
