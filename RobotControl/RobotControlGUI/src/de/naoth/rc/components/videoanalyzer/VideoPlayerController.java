package de.naoth.rc.components.videoanalyzer;

import de.naoth.rc.Helper;
import java.io.File;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import javafx.util.StringConverter;

/**
 * FXML Controller class
 *
 * @author thomas
 */
public class VideoPlayerController implements Initializable
{

  public static final String FXML = "VideoPlayer.fxml";

  @FXML
  private MediaView mediaView;
  @FXML
  private Pane mediaPane;

  @FXML
  private Slider timeSlider;

  @FXML
  private ToggleButton playButton;
  @FXML
  private TextField timeCodeText;
  
  @FXML
  private Label timeModeIndicator;
  
  @FXML
  private Button resetZoomButton;
  
  @FXML
  private Rectangle zoomPreview;

  private final BooleanProperty zoomedProperty = new SimpleBooleanProperty(false);
  
  private Media media;
  private MediaPlayer player;

  private VideoAnalyzerController analyzer;

  private final SliderChangedListener sliderChangeListener = new SliderChangedListener();

  private final double MAX_FRAME_LENGTH = 60.0;
  
  private Point2D zoomStartPoint = null;

  /**
   * Initializes the controller class.
   */
  @Override
  public void initialize(URL url, ResourceBundle rb)
  {
    playButton.setGraphic(new ImageView(getClass().getResource("/de/naoth/rc/res/play.png").toString()));
    Tooltip.install(playButton, new Tooltip("Play/Pause"));

    timeSlider.setLabelFormatter(new TickFormatter());
    timeSlider.focusedProperty().addListener(new ChangeListener<Boolean>()
    {

      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
      {
        if(analyzer != null && Objects.equals(newValue, Boolean.TRUE))
        {
          analyzer.setMode(SelectionMode.TIME);
        }
      }
    });
    timeCodeText.textProperty().bindBidirectional(timeSlider.valueProperty(), new TimeCodeConverter());
    timeCodeText.addEventFilter(KeyEvent.KEY_TYPED, (KeyEvent event) ->
    {
      
      if(event.getCharacter().matches("[ p]"))
      {
        // play shortcut
       togglePlay();
       event.consume();
      }
      else if(!event.getCharacter().matches("[0-9:,.]"))
      {
        // ignore this key
        event.consume();;
      }
    });
    
    mediaView.fitWidthProperty().bind(mediaPane.widthProperty());
    mediaView.fitHeightProperty().bind(mediaPane.heightProperty());
    mediaView.setPreserveRatio(true);
    
    resetZoomButton.disableProperty().bind(zoomedProperty.not());
    
  }

  public void togglePlay()
  {
    if (player != null)
    {
      if (player.getStatus() == MediaPlayer.Status.PLAYING)
      {
        pause();
      } else
      {
        play();
      }
    }
  }

  @FXML
  private void playPause(ActionEvent event)
  {
    if (player != null)
    {
      if (playButton.isSelected())
      {
        play();
      } else
      {
        pause();
      }
    }
  }
  
  @FXML
  private void mediaViewDragStart(MouseEvent evt)
  {
    if(!zoomedProperty.get())
    {
      if(zoomStartPoint == null)
      {
        zoomPreview.setVisible(true);
        zoomPreview.setFill(null);
        zoomPreview.setStroke(new Color(1.0, 0.0, 0.0, 1.0));
        zoomStartPoint = new Point2D(evt.getX(), evt.getY());
        zoomPreview.setTranslateX(evt.getX());
        zoomPreview.setTranslateY(evt.getY());
        zoomPreview.setWidth(0.0);
        zoomPreview.setHeight(0.0);

      }
      else
      {
        zoomPreview.setWidth(evt.getX()-zoomStartPoint.getX());
        zoomPreview.setHeight(evt.getY()-zoomStartPoint.getY());
      }
    }
  }
  
  private Point2D getVideoCoordinates(Point2D paneCoordinates)
  {
    Point2D offset = new Point2D(mediaView.getX(), mediaView.getY());

    double ratio = 1.0;
    
    if(media != null)
    {
      double scaleHeight = ((double) media.getHeight()) / mediaView.getFitHeight();
      double scaleWidth = ((double) media.getWidth()) / mediaView.getFitWidth();
      ratio = Math.max(scaleHeight, scaleWidth);
    }
    
    Point2D p= paneCoordinates.add(offset).multiply(ratio);
    
    return p;
  }
  
  @FXML
  private void mediaViewDragEnd(MouseEvent evt)
  {
    if(mediaView != null && zoomStartPoint != null)
    {
      Point2D videoStart = getVideoCoordinates(zoomStartPoint);
      Point2D videoEnd = getVideoCoordinates(
        new Point2D(zoomStartPoint.getX()+zoomPreview.getWidth(), 
          zoomStartPoint.getY()+zoomPreview.getHeight()));
      
      double width = Math.max(0.0, videoEnd.getX()-videoStart.getX());
      double height = Math.max(0.0, videoEnd.getY()-videoStart.getY());
      
      //clamp to media height
      width = Math.min(width, media.getWidth()-videoStart.getX());
      height = Math.min(height, media.getHeight()-videoStart.getY());
      
      Rectangle2D newViewPort = new Rectangle2D(videoStart.getX(), videoStart.getY(), 
        width, height);
      
      mediaView.setViewport(newViewPort);
      zoomedProperty.set(true);
    }
    
    zoomStartPoint = null;
    zoomPreview.setVisible(false);
  }
  
  @FXML
  private void mediaClicked(MouseEvent evt)
  {
    if(evt.getClickCount() == 2)
    {
      resetZoom();
    }
  }
  
  @FXML
  private void resetZoom()
  {
    if(mediaView != null && media != null)
    {
      mediaView.setViewport(new Rectangle2D(0.0, 0.0, media.getWidth(), media.getHeight()));
      zoomedProperty.set(false);
    }
  }
  

  public void setTime(double newTimeSeconds)
  {
    if (player != null)
    {
      pauseAndSeek(Duration.seconds(newTimeSeconds));
      updateGUIForTimeCode(Duration.seconds(newTimeSeconds));
    }
  }

  public void setAnalyzer(VideoAnalyzerController analyzer)
  {
    this.analyzer = analyzer;
  }

  public void open(File file)
  {
    try
    {
      media = new Media(file.toURI().toASCIIString());

      player = new MediaPlayer(media);
      player.setOnReady(new Runnable()
      {
        @Override
        public void run()
        {
          Duration total = player.getTotalDuration();
          if (total != Duration.UNKNOWN && total != Duration.INDEFINITE)
          {
            timeSlider.setMax(total.toSeconds());
            timeSlider.setShowTickMarks(true);
            timeSlider.setShowTickLabels(true);

            if (total.toMinutes() < 1.5)
            {
              timeSlider.setMajorTickUnit(5.0);
              timeSlider.setMinorTickCount(0);
            } else
            {
              timeSlider.setMajorTickUnit(60.0);
              timeSlider.setMinorTickCount(10);
            }
            
            double framesPerSecond = 30.0;
            Object framerateObject = media.getMetadata().get("framerate");
            if(framerateObject instanceof Double)
            {
              framesPerSecond = (Double) framerateObject;
            }
            timeSlider.setBlockIncrement(1.0/framesPerSecond);
          }
        }
      });
      player.setOnEndOfMedia(new Runnable()
      {

        @Override
        public void run()
        {
          player.stop();
          pauseAndSeek(Duration.ZERO);
          updateGUIForTimeCode(Duration.ZERO);
        }
      });

      player.currentTimeProperty().addListener(new CurrentTimeListener());

      mediaView.setMediaPlayer(player);

      timeSlider.valueProperty().addListener(sliderChangeListener);

    } catch (MediaException ex)
    {
      Helper.handleException("Could not create the video output", ex);
    }
  }

  private void pause()
  {
    pauseAndSeek(null);
  }

  private void pauseAndSeek(final Duration seek)
  {
    if (player != null)
    {
      boolean wasPaused = player.getStatus() == MediaPlayer.Status.PAUSED
        || player.getStatus() == MediaPlayer.Status.STOPPED
        || player.getStatus() == MediaPlayer.Status.READY;

      internalPrepareSeek(seek, wasPaused);

      if (!wasPaused)
      {
        player.pause();
      }

      playButton.setSelected(false);

    }
  }

  private void internalPrepareSeek(final Duration seek, boolean wasPaused)
  {
    if (seek == null)
    {
      return;
    }

    if (!wasPaused)
    {
      player.setOnPaused(new Runnable()
      {

        @Override
        public void run()
        {
          player.seek(seek);
          if (analyzer != null && analyzer.getMode() == SelectionMode.TIME)
          {
            analyzer.setLogFrameFromVideo(seek.toSeconds());
          }
          player.setOnPaused(null);
        }
      });
    } else
    {
      player.seek(seek);
      if (analyzer != null && analyzer.getMode() == SelectionMode.TIME)
      {
        analyzer.setLogFrameFromVideo(seek.toSeconds());
      }
    }
  }

  private void play()
  {
    if (player != null)
    {      
      player.play();
      playButton.setSelected(true);
      timeSlider.requestFocus();
            
      if(analyzer != null)
      {
        analyzer.setMode(SelectionMode.TIME);
      }
    }
  }

  private void updateGUIForTimeCode(Duration time)
  {
    if(time.lessThan(Duration.ZERO))
    {
      time = Duration.ZERO;
    }
    timeSlider.setValue(time.toSeconds());
  }

  public double getElapsedSeconds()
  {
    if (player != null)
    {
      return player.getCurrentTime().toSeconds();
    }
    return 0.0;
  }

  public Label getTimeModeIndicator()
  {
    return timeModeIndicator;
  }
  
  

  public static class TickFormatter extends StringConverter<Double>
  {

    @Override
    public String toString(Double object)
    {
      Duration elapsed = Duration.seconds(object);
      double minutes = Math.floor(elapsed.toMinutes());
      double seconds = elapsed.toSeconds() - (minutes * 60);

      return String.format("%02d:%02.0f", (int) minutes, seconds);

    }

    @Override
    public Double fromString(String string)
    {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
  }

  private class SliderChangedListener implements ChangeListener<Number>
  {

    @Override
    public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
    {

      final Duration duration = Duration.seconds(newValue.doubleValue());
      
      if (player != null)
      {
        boolean wasChangedManually = false;
        Duration playerDuration = player.getCurrentTime();

        double diff = Math.abs(duration.toMillis() - playerDuration.toMillis());

        if (diff > MAX_FRAME_LENGTH)
        {
          wasChangedManually = true;
        }
        else if(player.getStatus() == MediaPlayer.Status.PAUSED || player.getStatus() == MediaPlayer.Status.READY)
        {
          wasChangedManually = true;
        }
        
        if(wasChangedManually)
        {
          pauseAndSeek(duration);
        }

      }
    }
  }

  private class CurrentTimeListener implements ChangeListener<Duration>
  {

    @Override
    public void changed(ObservableValue<? extends Duration> observable, Duration oldValue, Duration newValue)
    {
      Platform.runLater(new Runnable()
      {
        @Override
        public void run()
        {
          updateGUIForTimeCode(newValue);
 
          if (analyzer != null && analyzer.getMode() == SelectionMode.TIME)
          {
            analyzer.setLogFrameFromVideo(newValue.toSeconds());
          }
        }
      });
    }
  }
  
  private static class TimeCodeConverter extends StringConverter<Number>
  {
    
    private final Pattern timcodePattern = 
      Pattern.compile("((?<minutes>[0-9]+)\\:)?(?<seconds>[0-9]+([.,][0-9]*)?)");

    @Override
    public
    String toString(Number object)
    {
      Duration time = Duration.seconds(object.doubleValue());
      double minutes = Math.floor(time.toMinutes());
      double seconds = time.toSeconds() - (minutes * 60);
      return String.format("%02d:%05.2f", (int) minutes, seconds);
    }

    @Override
    public
    Number fromString(String string)
    {
      Matcher m = timcodePattern.matcher(string);
      if(m.matches())
      {
        String minutesRaw = m.group("minutes");
        String secondsRaw = m.group("seconds");
        double minutes = 0.0;
        double seconds = 0.0;
        try
        {
          if(minutesRaw != null && !minutesRaw.isEmpty())
          {
            minutes = Double.parseDouble(minutesRaw);
          }
          if(secondsRaw != null && !secondsRaw.isEmpty())
          {
            seconds = Double.parseDouble(secondsRaw.replace(',', '.'));
          }
        }
        catch(NumberFormatException ex)
        {
          Helper.handleException("Could not parse timecode \"" + string + "\"", ex);
        }
        return (minutes*60.0) + seconds;
      }
      else
      {
        return 0.0;
      }
    }
    
  }
}
