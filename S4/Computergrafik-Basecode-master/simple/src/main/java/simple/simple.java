package simple;

import jrtr.*;
import javax.swing.*;
import java.awt.event.*;
import javax.vecmath.*;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Implements a simple application that opens a 3D rendering window and
 * shows a rotating cube.
 */
public class simple {
	static RenderPanel renderPanel;
	static RenderContext renderContext;
	static Shader normalShader, diffuseShader, phongShader, customShader;
	static Material material;
	static SimpleSceneManager sceneManager;
	static Shape shape;
	static float currentstep, basicstep;

	/**
	 * An extension of {@link GLRenderPanel} or {@link SWRenderPanel} to
	 * provide a call-back function for initialization. Here we construct
	 * a simple 3D scene and start a timer task to generate an animation.
	 */
	public final static class SimpleRenderPanel extends GLRenderPanel {
		/**
		 * Initialization call-back. We initialize our renderer here.
		 * 
		 * @param r
		 *            the render context that is associated with this render panel
		 */
		public void init(RenderContext r) {
			renderContext = r;

			// Make a simple geometric object: a cube

			// The vertex positions of the cube
			float v[] = { -1, -1, 1, 1, -1, 1, 1, 1, 1, -1, 1, 1,		// front face
					-1, -1, -1, -1, -1, 1, -1, 1, 1, -1, 1, -1,	// left face
					1, -1, -1, -1, -1, -1, -1, 1, -1, 1, 1, -1,		// back face
					1, -1, 1, 1, -1, -1, 1, 1, -1, 1, 1, 1,		// right face
					1, 1, 1, 1, 1, -1, -1, 1, -1, -1, 1, 1,		// top face
					-1, -1, 1, -1, -1, -1, 1, -1, -1, 1, -1, 1 };	// bottom face

			// The vertex normals 
			float n[] = { 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1,			// front face
					-1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0,		// left face
					0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1,		// back face
					1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0,			// right face
					0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0,			// top face
					0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0 };		// bottom face

			// The vertex colors
			float c[] = { 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0,
					1, 0, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0,
					1, 0, 0, 1 };

			// Texture coordinates 
			float uv[] = { 0, 0, 1, 0, 1, 1, 0, 1, 0, 0, 1, 0, 1, 1, 0, 1, 0, 0, 1, 0, 1, 1, 0, 1, 0, 0, 1, 0, 1, 1, 0, 1,
					0, 0, 1, 0, 1, 1, 0, 1, 0, 0, 1, 0, 1, 1, 0, 1 };

			// Construct a data structure that stores the vertices, their
			// attributes, and the triangle mesh connectivity
			VertexData vertexData = renderContext.makeVertexData(24);
			vertexData.addElement(c, VertexData.Semantic.COLOR, 3);
			vertexData.addElement(v, VertexData.Semantic.POSITION, 3);
			vertexData.addElement(n, VertexData.Semantic.NORMAL, 3);
			vertexData.addElement(uv, VertexData.Semantic.TEXCOORD, 2);

			// The triangles (three vertex indices for each triangle)
			int indices[] = { 0, 2, 3, 0, 1, 2,			// front face
					4, 6, 7, 4, 5, 6,			// left face
					8, 10, 11, 8, 9, 10,		// back face
					12, 14, 15, 12, 13, 14,	// right face
					16, 18, 19, 16, 17, 18,	// top face
					20, 22, 23, 20, 21, 22 };	// bottom face

			vertexData.addIndices(indices);
			shape = new Shape(vertexData); // cube

			int setting = 1;

			// Make a scene manager and add the object
			sceneManager = new SimpleSceneManager();

			// TODO: Add Lights

			if (setting == 0 || setting == 1) {
				Light light = new Light();
				light.position = new Vector3f(3, 0, 2);
				light.diffuse = new Vector3f(1, 0, 0);
				light.specular = new Vector3f(1, 0, 0);
				sceneManager.addLight(light);

				light = new Light();
				light.position = new Vector3f(0, 0, 3);
				sceneManager.addLight(light);
			}

			if (setting == 2) {
				Light light = new Light();
				light.position = new Vector3f(3, 0, 0);
				light.diffuse = new Vector3f(1, 0, 0);
				light.specular = new Vector3f(1, 0, 0);
				sceneManager.addLight(light);

				light = new Light();
				light.position = new Vector3f(0, 0, 3);
				light.diffuse = new Vector3f(1, 1, 1);
				light.specular = new Vector3f(1, 1, 1);
				sceneManager.addLight(light);
			}
			
			if (setting == 3) {
				Light light = new Light();
				light.position = new Vector3f(3, 0, 2);
				light.diffuse = new Vector3f(1, 0, 0);
				light.specular = new Vector3f(1, 0, 0);
				//sceneManager.addLight(light);

				light = new Light();
				light.position = new Vector3f(0, 0, 3);
				sceneManager.addLight(light);
			}
			
			
			// Add the scene to the renderer
			renderContext.setSceneManager(sceneManager);

			// Load some more shaders
			normalShader = renderContext.makeShader();
			try {
				normalShader.load("../jrtr/shaders/normal.vert", "../jrtr/shaders/normal.frag");
			} catch (Exception e) {
				System.out.print("Problem with shader:\n");
				System.out.print(e.getMessage());
			}

			diffuseShader = renderContext.makeShader();
			try {
				diffuseShader.load("../jrtr/shaders/diffuse.vert", "../jrtr/shaders/diffuse.frag");
			} catch (Exception e) {
				System.out.print("Problem with shader:\n");
				System.out.print(e.getMessage());
			}

			phongShader = renderContext.makeShader();
			try {
				phongShader.load("../jrtr/shaders/phong.vert", "../jrtr/shaders/phong.frag");
			} catch (Exception e) {
				System.out.print("Problem with shader:\n");
				System.out.print(e.getMessage());
			}
			
			customShader = renderContext.makeShader();
			try {
				customShader.load("../jrtr/shaders/custom.vert", "../jrtr/shaders/custom.frag");
			} catch (Exception e) {
				System.out.print("Problem with shader:\n");
				System.out.print(e.getMessage());
			}

			// TODO: Set Material

			// Make a material that can be used for shading
			material = new Material();
			material.shininess = 16;
			if (setting == 0 || setting == 1) {
				material.shader = diffuseShader;
			} else if (setting == 2) {
				material.shader = phongShader;
			} else if (setting == 3){
				material.shader = customShader;
			}
			material.texture = renderContext.makeTexture();
			try {
				material.texture.load("../textures/plant.jpg");
			} catch (Exception e) {
				System.out.print("Could not load texture.\n");
				System.out.print(e.getMessage());
			}
			
			Material plant = new Material();
			plant.shininess = 16;
			if (setting == 0 || setting == 1) {
				plant.shader = diffuseShader;
			} else if (setting == 2) {
				plant.shader = phongShader;
			} else if (setting == 3){
				plant.shader = customShader;
			}
			plant.texture = renderContext.makeTexture();
			try {
				plant.texture.load("../textures/plant.jpg");
			} catch (Exception e) {
				System.out.print("Could not load texture.\n");
				System.out.print(e.getMessage());
			}
			
			Material wood = new Material();
			wood.shininess = 16;
			if (setting == 0 || setting == 1) {
				wood.shader = diffuseShader;
			} else if (setting == 2) {
				wood.shader = phongShader;
			} else if (setting == 3){
				wood.shader = customShader;
			}
			wood.texture = renderContext.makeTexture();
			try {
				wood.texture.load("../textures/wood.jpg");
			} catch (Exception e) {
				System.out.print("Could not load texture.\n");
				System.out.print(e.getMessage());
			}
			
			//TODO: init shapes
			
			if (setting == 0){
				sceneManager.addShape(shape);
				shape.setMaterial(material);
			} else if (setting == 1) {
				Shape cube = shape;
				cube.setMaterial(plant);
				sceneManager.addShape(shape); // cube
				
				
				Cylinder cylinder = new Cylinder(1, 1, 50, renderContext, 3, 1, 1);
				shape = new Shape(cylinder.makeCylinder());
				shape.setMaterial(wood);
				sceneManager.addShape(shape);
			}

			else if (setting == 2) {
				sceneManager.addShape(shape);
			}
			
			else if (setting == 3){
				Shape cube = shape;
				cube.setMaterial(plant);
				sceneManager.addShape(shape); // cube
				
				
				Cylinder cylinder = new Cylinder(1, 1, 50, renderContext, 3, 1, 1);
				shape = new Shape(cylinder.makeCylinder());
				shape.setMaterial(wood);
				sceneManager.addShape(shape);
			}

			// Register a timer task
			Timer timer = new Timer();
			basicstep = 0.01f;
			currentstep = basicstep;
			timer.scheduleAtFixedRate(new AnimationTask(), 0, 10);
			
		}
	}

	/**
	 * A timer task that generates an animation. This task triggers
	 * the redrawing of the 3D scene every time it is executed.
	 */
	public static class AnimationTask extends TimerTask {
		public void run() {
			// Update transformation by rotating with angle "currentstep"
			Matrix4f t = shape.getTransformation();
			Matrix4f rotX = new Matrix4f();
			rotX.rotX(currentstep);
			Matrix4f rotY = new Matrix4f();
			rotY.rotY(currentstep);
			t.mul(rotX);
			t.mul(rotY);
			shape.setTransformation(t);

			// Trigger redrawing of the render window
			renderPanel.getCanvas().repaint();
		}
	}

	/**
	 * A mouse listener for the main window of this application. This can be
	 * used to process mouse events.
	 */
	public static class SimpleMouseListener implements MouseListener {
		public void mousePressed(MouseEvent e) {
		}

		public void mouseReleased(MouseEvent e) {
		}

		public void mouseEntered(MouseEvent e) {
		}

		public void mouseExited(MouseEvent e) {
		}

		public void mouseClicked(MouseEvent e) {
		}
	}

	/**
	 * A key listener for the main window. Use this to process key events.
	 * Currently this provides the following controls:
	 * 's': stop animation
	 * 'p': play animation
	 * '+': accelerate rotation
	 * '-': slow down rotation
	 * 'd': default shader
	 * 'n': shader using surface normals
	 * 'm': use a material for shading
	 */
	public static class SimpleKeyListener implements KeyListener {
		public void keyPressed(KeyEvent e) {
			switch (e.getKeyChar()) {
			case 's': {
				// Stop animation
				currentstep = 0;
				break;
			}
			case 'p': {
				// Resume animation
				currentstep = basicstep;
				break;
			}
			case '+': {
				// Accelerate roation
				currentstep += basicstep;
				break;
			}
			case '-': {
				// Slow down rotation
				currentstep -= basicstep;
				break;
			}
			case 'n': {
				// Remove material from shape, and set "normal" shader
				shape.setMaterial(null);
				renderContext.useShader(normalShader);
				break;
			}
			case 'd': {
				// Remove material from shape, and set "default" shader
				shape.setMaterial(null);
				renderContext.useDefaultShader();
				break;
			}
			case 'm': {
				// Set a material for more complex shading of the shape
				if (shape.getMaterial() == null) {
					shape.setMaterial(material);
				} else {
					shape.setMaterial(null);
					renderContext.useDefaultShader();
				}
				break;
			}
			}

			// Trigger redrawing
			renderPanel.getCanvas().repaint();
		}

		public void keyReleased(KeyEvent e) {
		}

		public void keyTyped(KeyEvent e) {
		}

	}

	/**
	 * The main function opens a 3D rendering window, implemented by the class
	 * {@link SimpleRenderPanel}. {@link SimpleRenderPanel} is then called backed
	 * for initialization automatically. It then constructs a simple 3D scene,
	 * and starts a timer task to generate an animation.
	 */
	public static void main(String[] args) {
		// Make a render panel. The init function of the renderPanel
		// (see above) will be called back for initialization.
		renderPanel = new SimpleRenderPanel();

		// Make the main window of this application and add the renderer to it
		JFrame jframe = new JFrame("simple");
		jframe.setSize(500, 500);
		jframe.setLocationRelativeTo(null); // center of screen
		jframe.getContentPane().add(renderPanel.getCanvas());// put the canvas into a JFrame window

		// Add a mouse and key listener
		renderPanel.getCanvas().addMouseListener(new SimpleMouseListener());
		renderPanel.getCanvas().addKeyListener(new SimpleKeyListener());
		renderPanel.getCanvas().setFocusable(true);

		jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jframe.setVisible(true); // show window
	}
}
