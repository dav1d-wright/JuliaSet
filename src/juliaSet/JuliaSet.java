package juliaSet;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;
import java.util.Random;

public class JuliaSet extends JFrame implements ActionListener
{
	private JButton m_cBStart;
	private JTextField m_cTReal;
	private JTextField m_cTImag;
	private JLabel m_cLReal;
	private JLabel m_cLImag;
	private String m_cSMsg = "c = ";
	private JuliaCanvas m_cCanvas;
	private int m_iPlotWidth; // number of cells
	private int m_iPlotHeight; // number of cells
	private Boolean m_bRunning;
	private double m_dReal= -0.8;
	private double m_dImag= 0.156;
	private Complex m_cCoordPlane[][];
	
	private static final int PLOTMAX = 2; // we'll have symmetric axes ((0,0) at the centre of the plot
	
	JuliaSet (String aTitle, int aFrameWidth, int aFrameHeight, int aPlotWidth, int aPlotHeight) 
	{
		super(aTitle);
		this.setSize(aFrameWidth, aFrameHeight);
		m_iPlotWidth = aPlotWidth ;
		m_iPlotHeight = aPlotHeight;
		m_bRunning = false;
		
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() 
        {
            public void windowClosing(WindowEvent e)
            {
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
		m_cLReal = new JLabel("Re(c):");
		m_cLImag = new JLabel("Im(c):");
		
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
		
		// m_cBStart
		cConstraints.gridx = 0;
		cConstraints.gridy = 3;
		cLayout.setConstraints(m_cBStart, cConstraints);
		this.add(m_cBStart);
		
		this.repaint();
		
		this.transformCoordinates();
	}
	
	public synchronized void stop() 
	{
		if (m_bRunning) 
		{
			m_bRunning = false;
			boolean bRetry = true;
			while (bRetry) 
			{
				// TODO implement rest of stop from stackexchange answer
			}
		}
	}
	
	public void paint (Graphics aGraphics)
	{
		m_cCanvas.setSize(new Dimension(m_iPlotWidth, m_iPlotHeight));
		this.paintComponents(aGraphics);
//			aGraphics.drawString("This is in frame window", 10, 400);
		aGraphics.drawString(m_cSMsg, 10, 450);
	}
	
	public void actionPerformed(ActionEvent aActionEvent) 
	{
		String strCmd = aActionEvent.getActionCommand();
		
		if(strCmd.equals("Start")) 
		{
			m_cCanvas.init();
			m_cSMsg = "Seed = " + Double.toString(m_dReal) + "j*" + Double.toString(m_dImag);
		}
		else if (aActionEvent.getSource() == m_cTReal)
		{
			m_dReal = Double.parseDouble(m_cTReal.getText());
		}
		else if (aActionEvent.getSource() == m_cTImag)
		{
			m_dImag = Double.parseDouble(m_cTImag.getText());
		}
		
		this.update(this.getGraphics());
	}
	
	public void transformCoordinates(){
		double dCanvasHeight = (double)m_cCanvas.getHeight();
		double dCanvasWidth = (double)m_cCanvas.getWidth();
		// init matrix with same amount of elements as pixels in canvas
		m_cCoordPlane = new Complex[(int)dCanvasHeight][(int)dCanvasWidth];
		int iPlotRange = 2*PLOTMAX;
		
		for(int i = 0; i < dCanvasHeight; i++){
			for(int j = 0; j < dCanvasWidth; j++){
				m_cCoordPlane[i][j] = new Complex((j / iPlotRange) - PLOTMAX, -((i / iPlotRange) - PLOTMAX));
			}
		}
		
	}
}

class JuliaCanvas extends Canvas 
{
	private int m_iWidth;
	private int m_iHeight;
	private Random m_cRnd;
	private BufferedImage m_cBackGroundImage = null;
	
	JuliaCanvas(int aWidth, int aHeight) 
	{
		m_iWidth = aWidth;
		m_iHeight = aHeight;
		m_cRnd = new Random();
		
		m_cRnd.setSeed(m_cRnd.nextLong());
		
		m_cBackGroundImage = new BufferedImage(m_iWidth, m_iHeight, BufferedImage.TYPE_INT_RGB);

	}
	
	public void init() 
	{

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
		
//		for(int i = 0; i < m_iWidth; i++) 
//		{
//			for(int j = 0; j < m_iHeight; j++) 
//			{
//				aGraphics.fillRect(m_cCells[i][j].getPixCoordXBegin(), m_cCells[i][j].getPixCoordYBegin()
//						, m_iCellWidth, m_iCellWidth);
//			}
//		}
		// rendering is done, draw background image to on screen graphics
		cScreenGraphics.drawImage(m_cBackGroundImage, 0, 0, null);
	}
	
	@Override
	public void update(Graphics aGraphics)
	{
		paint(aGraphics);
	}
}

class Complex
{
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
	
	public double getAbs()
	{
		return 0;
	}
}


