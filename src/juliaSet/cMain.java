package juliaSet;

import java.awt.Toolkit;
import java.awt.Dimension;

public class cMain {

	public static void main(String[] args) 
	{
//		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int windowWidth = 1500;//(int)screenSize.getWidth() - 200;
		int windowHeight = 1100;//(int)screenSize.getHeight() - 50;
		int plotWidth = 800;//(int)screenSize.getWidth() - 600;
		int plotHeight = 800;//(int)screenSize.getHeight() - 150;
		
		JuliaSet cJuliaSet = new JuliaSet("Julia Set", windowWidth, windowHeight, plotWidth, plotHeight);		
		
		cJuliaSet.setVisible(true);
		while(true){
//			JuliaSet.cCanvas.countNeighbours();
//			JuliaSet.cCanvas.calcNextStep();
//			JuliaSet.cCanvas.update(cGameOfLife.cCanvas.getGraphics());			
//			for(long i = 0; i < 0xAFFFFFFL; i++){
//				int x = 5;
//			};
		}
	}

}
