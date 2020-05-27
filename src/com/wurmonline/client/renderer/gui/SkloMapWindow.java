package com.wurmonline.client.renderer.gui;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import com.wurmonline.client.game.TerrainChangeListener;
import com.wurmonline.client.game.World;

public class SkloMapWindow implements TerrainChangeListener
{
    final World mWorld;
    private BufferedImage mMap;
	private JFrame mFrame = new JFrame("Transparent Window");
	private JLabel mLabel;
	private ImageIcon mIcon;
	private static SkloMapWindow mInstance;
	public static Logger mLogger = Logger.getLogger( "SkloMapWindow" );
	
	public SkloMapWindow( World pWorld)
	{
		this.mWorld = pWorld;
		this.mWorld.getNearTerrainBuffer().addListener( this );
	}
	
	public void start() 
	{
        if ( mInstance != null ) 
        {
        	SkloMapWindow.mInstance.mFrame.setVisible(false);
        	SkloMapWindow.mInstance.mFrame.dispose();
        }
        mInstance = this;
        this.mFrame = new JFrame( "Sklo Map Window" );        
        try 
        {
            String lPath = "https://sklotopolis.ddns.net/unlimited/2/mapdump-flat.png";
            URL lURL = new URL( lPath );
            BufferedImage lTmp = ImageIO.read( lURL );
            Image thumbnail = lTmp.getScaledInstance( 512, 512, Image.SCALE_SMOOTH);
            mMap = new BufferedImage(thumbnail.getWidth(null), thumbnail.getHeight(null), BufferedImage.TYPE_INT_RGB);
            mMap.getGraphics().drawImage(thumbnail, 0, 0, null);
            mIcon = new ImageIcon( mMap );
            mLabel = new JLabel( mIcon );
            mFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            mFrame.getContentPane().add( mLabel );
            mFrame.pack();
            mFrame.setLocation(200, 200);
            mFrame.setAlwaysOnTop( true );
            mFrame.setVisible(true);
            mFrame.setResizable(false);
            mFrame.setAlwaysOnTop( true );
            mFrame.setDefaultCloseOperation( JFrame.HIDE_ON_CLOSE );
            
        } 
        catch (Exception exp) 
        {   	
            exp.printStackTrace();
        }
        updatePosition();
    }
	
	public void updatePosition()
	{
		int lPX = mWorld.getPlayerCurrentTileX() / 8;
		int lPY = mWorld.getPlayerCurrentTileY() / 8;
		int[] lColor = { 255, 20 , 147 };
		BufferedImage lMap = mMap;
		for ( int i = lPX - 2; i <= lPX + 2; i ++ )
		{
			for ( int j = lPY - 2; j <= lPY + 2; j ++ )
			{
				mLogger.log( Level.INFO, "X/Y:" +i + "/" + j );
				lMap.getRaster().setPixel(i, j, lColor );			
			}
		}
		mIcon = new ImageIcon( lMap );
		mLabel.setIcon( mIcon );
		mLabel.repaint();
		mLogger.log( Level.INFO, "UPDATE POSITION" );
	}

	@Override
	public void terrainUpdated(int arg0, int arg1, int arg2, int arg3, boolean arg4, boolean arg5) 
	{
		mLogger.log( Level.INFO, "updated terrain" );
		updatePosition();
	}
}