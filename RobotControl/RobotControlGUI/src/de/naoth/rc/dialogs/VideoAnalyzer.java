package de.naoth.rc.dialogs;

import de.naoth.rc.RobotControl;
import de.naoth.rc.components.videoanalyzer.VideoAnalyzerController;
import de.naoth.rc.components.videoanalyzer.VideoPlayerController;
import de.naoth.rc.core.dialog.AbstractJFXDialog;
import de.naoth.rc.core.dialog.DialogPlugin;
import de.naoth.rc.core.dialog.RCDialog;
import de.naoth.rc.core.manager.SwingCommandExecutor;
import de.naoth.rc.logmanager.LogFileEventManager;
import java.io.Serializable;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.util.Duration;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.injections.InjectPlugin;

/**
 *
 * @author thomas
 */
public class VideoAnalyzer extends AbstractJFXDialog
{

  @RCDialog(category = RCDialog.Category.Tools, name = "VideoAnalyzer")
  @PluginImplementation
  public static class Plugin extends DialogPlugin<VideoAnalyzer>
  {
    @InjectPlugin
    public static RobotControl parent;
    @InjectPlugin
    public static SwingCommandExecutor commandExecutor;
    @InjectPlugin
    public static LogFileEventManager logFileEventManager;
  }

  public static class GameStateChange implements Serializable
  {

    public double time;
    public String state;

    @Override
    public String toString()
    {
      Duration elapsed = Duration.seconds(time);
      double minutes = Math.floor(elapsed.toMinutes());
      double seconds = elapsed.toSeconds() - (minutes * 60);

      return String.format("%s (at %02d:%02.0f)", state, (int) minutes, seconds);
    }
  }

  public final static String KEY_VIDEO_FILE = "video-file";
  public final static String KEY_SYNC_TIME_VIDEO = "sync-time-video";
  public final static String KEY_SYNC_TIME_LOG = "sync-time-log";

  public VideoAnalyzer()
  {

  }

  @Override
  public URL getFXMLRessource()
  {
    return VideoPlayerController.class.getResource("VideoAnalyzer.fxml");
  }

  @Override
  public Map<KeyCombination, Runnable> getGlobalShortcuts()
  {
    HashMap<KeyCombination, Runnable> result = new HashMap<>();

    Runnable togglePlayRunnable = new Runnable()
    {

      @Override
      public void run()
      {
        VideoAnalyzerController c = VideoAnalyzer.this.<VideoAnalyzerController>getController();
        c.togglePlay();
      }
    };

    result.put(new KeyCodeCombination(KeyCode.SPACE), togglePlayRunnable);
    result.put(new KeyCodeCombination(KeyCode.P), togglePlayRunnable);

    return result;
  }

}
