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

public class ExternalMapWindow implements TerrainChangeListener
{
    final World mWorld;
    final private int mSize;
    private BufferedImage mMap;
    private BufferedImage mOriginal;
	private JFrame mFrame = new JFrame("Transparent Window");
	private JLabel mLabel;
	private ImageIcon mIcon;
	private Image mThumbnail;
	private String mPath;
	private String mServerName;
	private double mDividor;
	private int mServerSize = 4096;
	private final String[] mServerNames;
	private final String[] mMapLinks;
	private final int[] mServerSizes;
	private static ExternalMapWindow mInstance;
	public static Logger mLogger = Logger.getLogger( "External Map Window" );
	
	public ExternalMapWindow( World pWorld, int pSize, String[] pServerNames, String[] pMapLinks, int[] pServerSizes )
	{
		this.mWorld = pWorld;
		this.mWorld.getNearTerrainBuffer().addListener( this );
		this.mServerName = pWorld.getServerName();
		this.mSize = pSize;
		this.mServerNames = pServerNames;
		this.mMapLinks = pMapLinks;
		this.mServerSizes = pServerSizes;
	}
	
	public void start() 
	{
        if ( mInstance != null ) 
        {
        	ExternalMapWindow.mInstance.mFrame.setVisible(false);
        	ExternalMapWindow.mInstance.mFrame.dispose();
        }
        mInstance = this;
        this.mFrame = new JFrame( "Sklo Map Window" );        
        try 
        {
            loadMap( mWorld.getServerName() );
            mThumbnail = mOriginal.getScaledInstance( mSize, mSize, Image.SCALE_SMOOTH );
            mMap = new BufferedImage( mThumbnail.getWidth( null ), mThumbnail.getHeight( null ), BufferedImage.TYPE_INT_RGB );
            mMap.getGraphics().drawImage( mThumbnail, 0, 0, null );
            mIcon = new ImageIcon( mMap );
            mLabel = new JLabel( mIcon );
            mFrame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
            mFrame.getContentPane().add( mLabel );
            mFrame.pack();
            mFrame.setLocation( 200, 200 );
            mFrame.setAlwaysOnTop( true );
            mFrame.setVisible( true );
            mFrame.setResizable( false );
            mFrame.setAlwaysOnTop( true );
            mFrame.setDefaultCloseOperation( JFrame.HIDE_ON_CLOSE );
            
        } 
        catch (Exception exp) 
        {   	
            exp.printStackTrace();
        } catch (Throwable e) 
        {
			e.printStackTrace();
		}
        updatePosition();
    }
	
	private void loadMap( String pServername ) throws Throwable
	{
		mLogger.log(Level.INFO, mWorld.getServerName() );
		for (  int i = 0; i < mServerNames.length; i ++ )
		{
			if ( mServerName.equalsIgnoreCase( mServerNames[i] ) )
			{
				mPath = mMapLinks[i];
				mServerSize = mServerSizes[i];
			}
		}
		
		mDividor = (double)mServerSize / (double)mSize;
		
        URL lURL = new URL( mPath );
        mOriginal = ImageIO.read( lURL );
	}
	
	private void updatePosition()
	{
		if ( !mServerName.contentEquals( mWorld.getServerName() ) )
		{
			try 
			{
				mServerName = mWorld.getServerName();
				loadMap( mWorld.getServerName() );
			} catch (Throwable e) 
			{
				e.printStackTrace();
			}
		}

		int lPX = (int)( (double)mWorld.getPlayerCurrentTileX() / mDividor );
		int lPY = (int)( (double)mWorld.getPlayerCurrentTileY() / mDividor );
		int[] lColor = { 255, 20 , 147 };
		BufferedImage lMap = new BufferedImage( mThumbnail.getWidth( null ), mThumbnail.getHeight( null ), BufferedImage.TYPE_INT_RGB );
        lMap.getGraphics().drawImage( mThumbnail, 0, 0, null );
		for ( int i = lPX - 2; i <= lPX + 2; i ++ )
		{
			for ( int j = lPY - 2; j <= lPY + 2; j ++ )
			{
				lMap.getRaster().setPixel(i, j, lColor );			
			}
		}
		mIcon = new ImageIcon( lMap );
		mLabel.setIcon( mIcon );
        mFrame.setTitle( "Sklotopolis - " + mServerName + " Map ( " + mWorld.getPlayerCurrentTileX() + "/" + mWorld.getPlayerCurrentTileY() + " )" );
		mLabel.repaint();
	}

	@Override
	public void terrainUpdated(int arg0, int arg1, int arg2, int arg3, boolean arg4, boolean arg5) 
	{
		updatePosition();
	}
}