package juliaSet;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;
import java.util.Random;
import java.io.*;
import java.lang.ref.*;

public class JuliaSet extends JFrame implements Runnable, ActionListener 
{
	private JButton m_cBStart;
	private JTextField m_cTReal;
	private JTextField m_cTImag;
	private JTextField m_cTDivergThresh;
	private JLabel m_cLReal;
	private JLabel m_cLImag;
	private JLabel m_cLDivergThresh;
	private int m_iDivergThresh = 10;
	private String m_cMsgDivThresh = "Divergence threshold = " + m_iDivergThresh;
	private JuliaCanvas m_cCanvas;
	private int m_iPlotWidth; // number of cells
	private int m_iPlotHeight; // number of cells
	private Boolean m_bRunning = false;
	private double m_dReal = 0.3;
	private double m_dImag = -0.5;
	private String m_cSMsg = "c = " + Double.toString(m_dReal) + " + " + "j*" + Double.toString(m_dImag);
	private String m_cMsgIter = "x = 0, y = 0";
	private Complex m_cCoordPlane[][];
	private double m_dAbsSqValues[][];
	private int m_iIterations[][];
	private Complex m_cSummand;
	private BufferedImage m_cBackGroundImage = null;
	private FileWriter m_cFileWriter;
	private BufferedWriter m_cBufferedWriter;
	private String m_sFileName = "log.txt";
	private Boolean m_bWriteLog = false;

	private static final double PLOTMAX = 2.0; // we'll have symmetric axes
												// ((0,0) at the centre of the
												// plot
	private static final int MAXITER = 0xff;

	JuliaSet(String aTitle, int aFrameWidth, int aFrameHeight, int aPlotWidth, int aPlotHeight) 
	{
		super(aTitle);
		this.setSize(aFrameWidth, aFrameHeight);
		m_iPlotWidth = aPlotWidth;
		m_iPlotHeight = aPlotHeight;
		m_cSummand = new Complex(m_dReal, m_dImag);

		m_cBackGroundImage = new BufferedImage(aFrameWidth, aFrameHeight, BufferedImage.TYPE_INT_RGB);

		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				stop();
				super.windowClosing(e);
				System.exit(0);
			}
		});

		GridBagLayout cLayout = new GridBagLayout();
		GridBagConstraints cConstraints = new GridBagConstraints();

		this.setLayout(cLayout);
		m_cCanvas = new JuliaCanvas(m_iPlotWidth, m_iPlotHeight);
		m_cCanvas.setSize(m_iPlotWidth, m_iPlotHeight);

		m_cBStart = new JButton("Start");
		m_cBStart.addActionListener(this);
		m_cTReal = new JTextField(5);
		m_cTReal.addActionListener(this);
		m_cTImag = new JTextField(5);
		m_cTImag.addActionListener(this);
		m_cTDivergThresh = new JTextField(5);
		m_cTDivergThresh.addActionListener(this);
		m_cLReal = new JLabel("Re(c):");
		m_cLImag = new JLabel("Im(c):");
		m_cLDivergThresh = new JLabel("Divergence Threshold:");

		cConstraints.insets.top = 3;
		cConstraints.insets.bottom = 3;
		cConstraints.insets.right = 3;
		cConstraints.insets.left = 3;

		// cCanvas
		cConstraints.gridx = 0;
		cConstraints.gridy = 0;
		cLayout.setConstraints(m_cCanvas, cConstraints);
		this.add(m_cCanvas);

		// m_cLReal
		cConstraints.gridx = 0;
		cConstraints.gridy = 1;
		cLayout.setConstraints(m_cLReal, cConstraints);
		this.add(m_cLReal);

		// m_cTReal
		cConstraints.gridx = 1;
		cConstraints.gridy = 1;
		cLayout.setConstraints(m_cTReal, cConstraints);
		this.add(m_cTReal);

		// m_cLImag
		cConstraints.gridx = 0;
		cConstraints.gridy = 2;
		cLayout.setConstraints(m_cLImag, cConstraints);
		this.add(m_cLImag);

		// m_cTImag
		cConstraints.gridx = 1;
		cConstraints.gridy = 2;
		cLayout.setConstraints(m_cTImag, cConstraints);
		this.add(m_cTImag);

		// m_cLDivergThresh
		cConstraints.gridx = 0;
		cConstraints.gridy = 3;
		cLayout.setConstraints(m_cLDivergThresh, cConstraints);
		this.add(m_cLDivergThresh);

		// m_cTDivergThresh
		cConstraints.gridx = 1;
		cConstraints.gridy = 3;
		cLayout.setConstraints(m_cTDivergThresh, cConstraints);
		this.add(m_cTDivergThresh);

		// m_cBStart
		cConstraints.gridx = 0;
		cConstraints.gridy = 4;
		cLayout.setConstraints(m_cBStart, cConstraints);
		this.add(m_cBStart);
		if (m_bWriteLog) 
		{
			try 
			{
				m_cFileWriter = new FileWriter(m_sFileName, false);
				m_cBufferedWriter = new BufferedWriter(m_cFileWriter);
			} catch (IOException ex) {
				System.out.println("Error opening file '" + m_sFileName + "'");
			}
		}		
		this.repaint();
//		this.setVisible(true);
		this.transformCoordinates();
	}

	public synchronized void stop() 
	{
		if (m_bRunning) 
		{
			m_bRunning = false;
			boolean bRetry = true;
		}
		if (m_bWriteLog) 
		{
			try {
				m_cBufferedWriter.close();
				m_cFileWriter.close();
			} catch (IOException ex) {
				System.out.println("Error closing file '" + m_sFileName + "'");
			}
		}
	}
	public void collectGarbage() 
	{
	    Object cObj = new Object();
	    WeakReference ref = new WeakReference<Object>(cObj);
	    cObj = null;
	    while(ref.get() != null) {
	    	System.gc();
	    }
	}
	   
	public void setSummand(Complex aSummand) 
	{
		m_cSummand.setIm(aSummand.getIm());
		m_dImag = aSummand.getIm();
		m_cSummand.setRe(aSummand.getRe());
		m_dReal = aSummand.getRe();
		m_cSMsg = "c = " + Double.toString(m_dReal) + " + " + "j*" + Double.toString(m_dImag);
	}

	public void paint(Graphics aGraphics) 
	{
		Graphics cScreenGraphics = aGraphics;
		// render on background image
		aGraphics = m_cBackGroundImage.getGraphics();

		this.paintComponents(aGraphics);
		// aGraphics.drawString("This is in frame window", 10, 400);
		aGraphics.setColor(Color.BLACK);
		aGraphics.drawString(m_cSMsg, 10, 450);
		aGraphics.drawString(m_cMsgIter, 10, 465);
		aGraphics.drawString(m_cMsgDivThresh, 10, 480);

		// rendering is done, draw background image to on screen graphics
		cScreenGraphics.drawImage(m_cBackGroundImage, 0, 0, null);
	}

	public void actionPerformed(ActionEvent aActionEvent) 
	{
		String strCmd = aActionEvent.getActionCommand();

		if (strCmd.equals("Start")) 
		{
			m_cCanvas.init();
			m_cSMsg = "c = " + Double.toString(m_dReal) + " + " + "j*" + Double.toString(m_dImag);
			m_bRunning = true;
			this.run();
		} 
		else if (aActionEvent.getSource() == m_cTReal) 
		{
			m_dReal = Double.parseDouble(m_cTReal.getText());
			m_cSMsg = "c = " + Double.toString(m_dReal) + " + " + "j*" + Double.toString(m_dImag);
			m_cSummand.setRe(m_dReal);
		} 
		else if (aActionEvent.getSource() == m_cTImag) 
		{
			m_dImag = Double.parseDouble(m_cTImag.getText());
			m_cSMsg = "c = " + Double.toString(m_dReal) + " + " + "j*" + Double.toString(m_dImag);
			m_cSummand.setIm(m_dImag);
		} 
		else if (aActionEvent.getSource() == m_cTDivergThresh) 
		{
			m_iDivergThresh = Integer.parseInt(m_cTDivergThresh.getText());
			m_cMsgDivThresh = "Divergence threshold = " + m_iDivergThresh;
		}

		this.update(this.getGraphics());
	}

	public void transformCoordinates() 
	{
		double dCanvasHeight = (double) m_cCanvas.getHeight();
		double dCanvasWidth = (double) m_cCanvas.getWidth();
		// init matrix with same amount of elements as pixels in canvas
		m_cCoordPlane = new Complex[(int) dCanvasHeight][(int) dCanvasWidth];
		double iPlotRange = 2 * PLOTMAX;

		for (int i = 0; i < dCanvasHeight; i++) 
		{
			for (int j = 0; j < dCanvasWidth; j++) 
			{

				m_cCoordPlane[i][j] = new Complex((i - (dCanvasWidth / 2)) * iPlotRange / dCanvasWidth,
						(j - (dCanvasHeight / 2)) * iPlotRange / dCanvasHeight);
			}
		}

	}

	public void calcAbsSqValues() 
	{
		int iCanvasHeight = m_cCanvas.getHeight();
		int iCanvasWidth = m_cCanvas.getWidth();
		// init matrix with same amount of elements as pixels in canvas
		m_dAbsSqValues = new double[iCanvasHeight][iCanvasWidth];
		m_iIterations = new int[iCanvasHeight][iCanvasWidth];
		Complex cSum = new Complex();

		if (m_bWriteLog) {
			try 
			{
				m_cBufferedWriter.write("m_iIterations[][] =");
				m_cBufferedWriter.newLine();
			} 
			catch (IOException ex) 
			{
				System.out.println("Error opening file '" + m_sFileName + "'");
			}
		}

		for (int i = 0; i < iCanvasHeight; i++) 
		{
			for (int j = 0; j < iCanvasWidth; j++) 
			{
				cSum.setRe(m_cCoordPlane[i][j].getRe());
				cSum.setIm(m_cCoordPlane[i][j].getIm());
				m_iIterations[i][j] = 0;
				do 
				{
					m_iIterations[i][j]++;
					cSum.square();
					cSum.add(m_cSummand);
					m_dAbsSqValues[i][j] = cSum.getAbsSq();
				} while ((m_iIterations[i][j] < MAXITER) && (m_dAbsSqValues[i][j] < m_iDivergThresh));
				this.calcColour(i, j, m_iIterations[i][j]);
				m_cMsgIter = "x = " + i + " , y = " + j;

				 if(m_bWriteLog)
				 {
					System.out.println(m_cMsgIter);
					System.out.flush();
				 }

				if (m_bWriteLog) {
					try 
					{
						m_cBufferedWriter.write(Integer.toString(m_iIterations[i][j]));
						m_cBufferedWriter.write(" ");
					} 
					catch (IOException ex) {
						System.out.println("Error writing to file '" + m_sFileName + "'");
					}
				}
			}
			if (m_bWriteLog) {
				try 
				{
					m_cBufferedWriter.newLine();
				} 
				catch (IOException ex) {
					System.out.println("Error writing to file '" + m_sFileName + "'");
				}
			}
		}
		m_dAbsSqValues = null;
		m_iIterations = null;
		cSum = null;
	}

	private void calcColour(int i, int j, int aIterations) 
	{
		Color cColour = Color.getHSBColor((int) Math.pow(aIterations, 4), 0xff,
				0xff * ((aIterations < MAXITER) ? 1 : 0));
		m_cCanvas.setPixelColour(i, j, cColour);
		cColour = null;
	}

	private void handleCalculation()
	{
		Complex cSummand = new Complex();
		
		for(int i = -800; i <= 800; i++)
		{
			for(int j = -800; j <= 800; j++)
			{
				cSummand.setRe(((double)i)/1000.0);
				cSummand.setIm(((double)j)/1000.0);
				this.setSummand(cSummand);
				this.calcAbsSqValues();
				this.getCanvas().paint(m_cCanvas.getGraphics());
				this.paint(this.getGraphics());
			}
		}
		cSummand = null;
		this.collectGarbage();
		System.gc();
		System.runFinalization();
	}
	
	public boolean isRunning() 
	{
		return m_bRunning;
	}

	public void setRunning(boolean aRunning) 
	{
		m_bRunning = aRunning;
	}
	
	public Canvas getCanvas()
	{
		return m_cCanvas;
	}
	
	public void run()
	{
	    if(m_bRunning)
	    {
	    	new Thread()
	    	{
            	@Override
                public void run() 
            	{
                    JuliaSet.this.handleCalculation();
                    JuliaSet.this.setVisible(false);
                }
            }.start();
            
	    }
	}
}

class JuliaCanvas extends Canvas 
{
	private int m_iWidth;
	private int m_iHeight;
	private Random m_cRnd;
	private BufferedImage m_cBackGroundImage = null;
	private int m_iRed[][];
	private int m_iGreen[][];
	private int m_iBlue[][];

	JuliaCanvas(int aWidth, int aHeight) 
	{
		m_iWidth = aWidth;
		m_iHeight = aHeight;
		m_cRnd = new Random();

		m_cRnd.setSeed(m_cRnd.nextLong());

		m_cBackGroundImage = new BufferedImage(m_iWidth, m_iHeight, BufferedImage.TYPE_INT_RGB);

		m_iRed = new int[m_iHeight][m_iWidth];
		m_iGreen = new int[m_iHeight][m_iWidth];
		m_iBlue = new int[m_iHeight][m_iWidth];
	}

	public void init() {

	}

	public void setPixelColour(int i, int j, Color aColour) 
	{
		m_iRed[i][j] = aColour.getRed();
		m_iGreen[i][j] = aColour.getGreen();
		m_iBlue[i][j] = aColour.getBlue();
	}

	private int getRandomInt(double aProbability) 
	{
		return (m_cRnd.nextDouble() < aProbability) ? 1 : 0;
	}

	@Override
	public void paint(Graphics aGraphics) 
	{
		// store on screen graphics
		Graphics cScreenGraphics = aGraphics;
		// render on background image
		aGraphics = m_cBackGroundImage.getGraphics();
		

		for (int i = 0; i < m_iWidth; i++) 
		{
			for (int j = 0; j < m_iHeight; j++) 
			{
				Color cColor = new Color(m_iRed[i][j], m_iGreen[i][j], m_iBlue[i][j]);
				aGraphics.setColor(cColor);
				aGraphics.drawRect(i, j, 0, 0);
				cColor = null;
			}
		}
		// rendering is done, draw background image to on screen graphics
		cScreenGraphics.drawImage(m_cBackGroundImage, 1, 1, null);
	}

	@Override
	public void update(Graphics aGraphics) 
	{
		paint(aGraphics);
	}
}

class Complex {
	private double m_dRe;
	private double m_dIm;

	public Complex() 
	{
		m_dRe = 0;
		m_dIm = 0;
	}

	public Complex(double aRe, double aIm)
	{
		m_dRe = aRe;
		m_dIm = aIm;
	}

	public Complex(Complex aComplex)
	{
		m_dRe = aComplex.m_dRe;
		m_dIm = aComplex.m_dIm;
	}

	public double getRe() {
		return m_dRe;
	}

	public void setRe(double adRe)
	{
		m_dRe = adRe;
	}

	public double getIm() {
		return m_dIm;
	}

	public void setIm(double adIm)
	{
		m_dIm = adIm;
	}

	public void add(Complex acComplex) 
	{
		 m_dRe += acComplex.getRe();
		 m_dIm += acComplex.getIm();
	}

	public void square() 
	{
		double m_dReSave = m_dRe;
		m_dRe =  (m_dRe * m_dRe) - (m_dIm * m_dIm);
		m_dIm = 2 * m_dReSave * m_dIm;
	}

	public double getAbsSq()
	{
		return ((m_dRe * m_dRe) + (m_dIm * m_dIm));
	}
}
