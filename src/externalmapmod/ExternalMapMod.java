package externalmapmod;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.gotti.wurmunlimited.modloader.ReflectionUtil;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;
import org.gotti.wurmunlimited.modloader.interfaces.Initable;
import org.gotti.wurmunlimited.modloader.interfaces.WurmClientMod;

import com.wurmonline.client.game.World;
import com.wurmonline.client.renderer.gui.HeadsUpDisplay;
import com.wurmonline.client.renderer.gui.ExternalMapWindow;

import javassist.ClassPool;
import javassist.CtClass;

public class ExternalMapMod implements WurmClientMod, Initable
{
	public static HeadsUpDisplay mHud;
	public static World mWorld;
	public static Logger mLogger = Logger.getLogger( "ExternalMapMod" );
	public static int mSize = 256;
	public static String[] mServerNames = { "Login", "Liberty", "Novus", "Caza" };
	public static String[] mMapLinks = { "https://sklotopolis.ddns.net/unlimited/1/mapdump-flat.png", "https://sklotopolis.ddns.net/unlimited/2/mapdump-flat.png", "https://sklotopolis.ddns.net/unlimited/3/mapdump-flat.png", "https://sklotopolis.ddns.net/unlimited/4/mapdump-flat.png" };
	public static int[] mServerSizes = { 1024, 4096, 4096, 2048 };
	
	public static boolean handleInput( final String pCommand, final String[] pData ) 
	{
		if ( pCommand.toLowerCase().equals( "toggleexternalmap" ) ) 
		{
			if ( pData.length == 2) 
			{
				mSize = Integer.parseInt( pData[1] );
				( new ExternalMapWindow( mWorld, mSize, mServerNames, mMapLinks, mServerSizes ) ).start();
				return true;				
			}
		}
		return false;
	}

	@Override
	public void init() 
	{
		mLogger.log( Level.INFO, "Init ExternalMapMod" );
		if ( mServerNames.length == mMapLinks.length )
		{
			try 
			{
				ClassPool lClassPool = HookManager.getInstance().getClassPool();
				
				CtClass lCtWurmConsole = lClassPool.getCtClass( "com.wurmonline.client.console.WurmConsole" );
				lCtWurmConsole.getMethod( "handleDevInput", "(Ljava/lang/String;[Ljava/lang/String;)Z" ).insertBefore("if (externalmapmod.ExternalMapMod.handleInput($1,$2)) return true;");
				
				HookManager.getInstance().registerHook( "com.wurmonline.client.renderer.gui.HeadsUpDisplay", "init", "(II)V", () -> ( pProxy, pMethod, pArgs ) -> 
				{
					pMethod.invoke( pProxy, pArgs );
					mHud = ( HeadsUpDisplay ) pProxy;
					return null;
				});
				
				HookManager.getInstance().registerHook( "com.wurmonline.client.renderer.WorldRender", "renderPickedItem", "(Lcom/wurmonline/client/renderer/backend/Queue;)V", () -> ( pProxy, pMethod, pArgs ) -> 
				{
					pMethod.invoke(pProxy, pArgs);
					Class<?> lCls = pProxy.getClass();
					
					mWorld = ReflectionUtil.getPrivateField( pProxy, ReflectionUtil.getField( lCls, "world" ) );
					
					return null;
				});
			} 
			catch ( Throwable e ) 
			{
				mLogger.log( Level.SEVERE, "Error TestMod", e.getMessage() );
			}			
		}
		else
		{
			mLogger.log( Level.WARNING, "ExternalMapMod couldn't initialized. Servernames/Maplinks haven't the same number." );
		}
	}
}