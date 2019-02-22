package com.aicas.jamaica.iot.demo.colorsorting.guiclient.fx;

import java.util.Collection;
import java.util.LinkedList;

import com.aicas.jamaica.iot.demo.colorsorting.guiclient.model.ColorSortingGUIModel;
import com.aicas.jamaica.iot.demo.colorsorting.guiclient.model.ColorSortingGUIModel.In.LightBarrierId;
import com.aicas.jamaica.iot.demo.colorsorting.guiclient.model.ColorSortingGUIModel.ValveId;
import com.aicas.jamaica.iot.demo.colorsorting.guiclient.model.impl.RandomColorSortingGUIModelImpl;
import com.aicas.jamaica.iot.demo.colorsorting.tools.ResourceLoader;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class ColorSortingGUIApplication extends Application
{

  private static final boolean DEBUG = false;

  private static final Insets INSETS5 = new Insets(5);
  private static final Insets INSETS10 = new Insets(10);
  private static final double SPACING10 = 10.00;

  private final ColorSortingGUIModel model;
  //private final XMPPClient xmppClient;
  private final Collection<Updateable> updateables;

  public ColorSortingGUIApplication()
  {
    /*/
     * model = new XMPPColorSortingGUIModelImpl();
     */
    model = new RandomColorSortingGUIModelImpl();
    //xmppClient = new XMPPClient(model);
    /*
     * System.out.println("XMPP");
     */
    updateables = new LinkedList<Updateable>();
  }

  @Override
  public void start(Stage stage)
    throws Exception
  {
    //xmppClient.start();
    stage.setTitle("Jamaica-IoT Color Sorting Demo");
    stage.setResizable(false);
    {
      BorderPane borderPane = new BorderPane();
      if (DEBUG)
      {
        borderPane.setStyle("-fx-border-style: solid");
        borderPane.setStyle("-fx-border-color: fuchsia");
      }
      ClassLoader classLoader = getClass().getClassLoader();
      Image bgImage = ResourceLoader.loadImage(classLoader, "HG_ganz-hinten.png");
      double sceneWidth = bgImage.getWidth();
      double sceneHeight = bgImage.getHeight();
      ResourceLoader.setImageBackground(borderPane, bgImage);

      Scene scene = new Scene(borderPane, sceneWidth, sceneHeight);
      {
        BorderPane topPane = new BorderPane();
        Image image = ResourceLoader.loadImage(classLoader, "headerstreifen_b.png");
        Image aicasLogoImage = ResourceLoader.loadImage(classLoader, "aicas-logo-redesign2018_RGB_78.png");

        double topWidth = image.getWidth();
        double topHeight = image.getHeight();
        topPane.setPrefSize(topWidth, topHeight);
        if (DEBUG) topPane.setStyle("-fx-border-style: solid");
        ResourceLoader.setImageBackground(topPane, image);
        {
          Text jamaicaIotText = new Text("Jamaica-IoT");
          double topFontSize = aicasLogoImage.getHeight();
          ResourceLoader.setFont(jamaicaIotText, classLoader, "aicasGothic-Demi.ttf", topFontSize);
          jamaicaIotText.setFill(Color.WHITE);
          topPane.setRight(jamaicaIotText);
          BorderPane.setMargin(jamaicaIotText, INSETS10);
        }
        {
          ImageView aicasLogoImageView = new ImageView(aicasLogoImage);
          topPane.setLeft(aicasLogoImageView);
          Insets insets = new Insets(15.00, 10.00, 25.00, 10.00);
          BorderPane.setMargin(aicasLogoImageView, insets);
        }
        borderPane.setTop(topPane);
      }

      {
        VBox leftVBox = new VBox();
        leftVBox.setPadding(INSETS5);
        leftVBox.setSpacing(SPACING10);
        String[] buttonLabels = new String[] { "Motor", "Compressor", "Cylinder 1", "Cylinder 2", "Cylinder 3"};
        double minWidth = 0;
        for (int i = 0; i < buttonLabels.length; i++)
          {
            Text text = new Text(buttonLabels[i]);
            double width = text.getLayoutBounds().getWidth();
            if (minWidth < width)
              {
                minWidth = width;
              }
          }
        {
          ToggleButton motorButton = new ToggleButton(buttonLabels[0]);
          motorButton.setMinWidth(minWidth + 20);
          EventHandler<ActionEvent> eventHandler = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event)
            {
              if (motorButton.isSelected())
                {
                  model.getOut().getMotor().rotate();
                }
              else
                {
                  model.getOut().getMotor().stop();
                }
            }
          };
          motorButton.setOnAction(eventHandler);
          leftVBox.getChildren().add(motorButton);
        }
        {
          ToggleButton compressorButton = new ToggleButton(buttonLabels[1]);
          compressorButton.setMinWidth(minWidth + 20);
          EventHandler<ActionEvent> eventHandler = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event)
            {
              if (compressorButton.isSelected())
                {
                  model.getOut().getCompressor().activate();
                }
              else
                {
                  model.getOut().getCompressor().stop();
                }
            }
          };
          compressorButton.setOnAction(eventHandler);
          leftVBox.getChildren().add(compressorButton);
        }
        {
          ButtonBase valve1Button = new Button(buttonLabels[2]);
          valve1Button.setMinWidth(minWidth + 20);
          EventHandler<ActionEvent> eventHandler = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event)
            {
              model.getOut().getValves().get(ValveId.WHITE).activate();
            }
          };
          valve1Button.setOnAction(eventHandler);
          leftVBox.getChildren().add(valve1Button);
        }
        {
          ButtonBase valve2Button = new Button(buttonLabels[3]);
          valve2Button.setMinWidth(minWidth + 20);
          EventHandler<ActionEvent> eventHandler = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event)
            {
              model.getOut().getValves().get(ValveId.RED).activate();
            }
          };
          valve2Button.setOnAction(eventHandler);
          leftVBox.getChildren().add(valve2Button);
        }
        {
          ButtonBase valve3Button = new Button(buttonLabels[4]);
          valve3Button.setMinWidth(minWidth + 20);
          EventHandler<ActionEvent> eventHandler = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event)
            {
              model.getOut().getValves().get(ValveId.BLUE).activate();
            }
          };
          valve3Button.setOnAction(eventHandler);
          leftVBox.getChildren().add(valve3Button);
        }
        borderPane.setLeft(leftVBox);
      }

      {
        StackPane centerStackPane = new StackPane();
        if (DEBUG)
          {
            centerStackPane.setStyle("-fx-border-style: solid");
          }
        {
          Image sortingLaneImage = ResourceLoader.loadImage(classLoader, "Sortierstrecke_600.png");
          ImageView sortingLaneImageView = new ImageView(sortingLaneImage);
          sortingLaneImageView.fitWidthProperty().bind(centerStackPane.widthProperty());
          centerStackPane.getChildren().add(sortingLaneImageView);
        }
        {
          Pane labelsPane = new Pane();
          DropShadow dropShadow = new DropShadow();
          dropShadow.setOffsetX(3.0f);
          dropShadow.setOffsetY(3.0f);
          dropShadow.setColor(Color.LIGHTSTEELBLUE);
          {
            Text compressorText = new Text("Compressor");
            compressorText.setFill(Color.WHITE);
            compressorText.setLayoutX(85);
            compressorText.setLayoutY(65);
            compressorText.setEffect(dropShadow);
            compressorText.setFont(Font.font(null, FontWeight.BOLD, 14));
            labelsPane.getChildren().add(compressorText);
          }
          {
            Text compressorText = new Text("Pneumatic Cylinders");
            compressorText.setFill(Color.WHITE);
            compressorText.setLayoutX(375);
            compressorText.setLayoutY(85);
            compressorText.setEffect(dropShadow);
            compressorText.setFont(Font.font(null, FontWeight.BOLD, 14));
            labelsPane.getChildren().add(compressorText);
          }
          {
            Text compressorText = new Text("Color Detector");
            compressorText.setFill(Color.WHITE);
            compressorText.setLayoutX(120);
            compressorText.setLayoutY(140);
            compressorText.setEffect(dropShadow);
            compressorText.setFont(Font.font(null, FontWeight.BOLD, 14));
            labelsPane.getChildren().add(compressorText);
          }
          {
            Text compressorText = new Text("Fischertechnik \n TXT Controller");
            compressorText.setFill(Color.WHITE);
            compressorText.setLayoutX(180);
            compressorText.setLayoutY(300);
            compressorText.setEffect(dropShadow);
            compressorText.setFont(Font.font(null, FontWeight.BOLD, 14));
            labelsPane.getChildren().add(compressorText);
          }
          centerStackPane.getChildren().add(labelsPane);
        }
        borderPane.setCenter(centerStackPane);
      }

      {
        VBox rightVBox = new VBox();
        rightVBox.setPrefSize(100.00, 460.00);
        rightVBox.setPadding(INSETS5);
        rightVBox.setSpacing(SPACING10);
        rightVBox.setAlignment(Pos.TOP_CENTER);
        Image lightBarrierImage = ResourceLoader.loadImage(classLoader, "LightBarrier.png");

        {
          ImageView lightBarrier1ImageView = new ImageView(lightBarrierImage);
          lightBarrier1ImageView.setFitHeight(80.00);
          lightBarrier1ImageView.setFitWidth(80.00);
          rightVBox.getChildren().add(lightBarrier1ImageView);
        }
        {
          Image colorSensorImage = ResourceLoader.loadImage(classLoader, "ColorSensor.png");
          ImageView colorSensorImageView = new ImageView(colorSensorImage);
          colorSensorImageView.setFitHeight(80.00);
          colorSensorImageView.setFitWidth(80.00);
          rightVBox.getChildren().add(colorSensorImageView);
        }
        {
          ImageView lightBarrier2ImageView = new ImageView(lightBarrierImage);
          lightBarrier2ImageView.setFitHeight(80.00);
          lightBarrier2ImageView.setFitWidth(80.00);
          rightVBox.getChildren().add(lightBarrier2ImageView);
        }
        borderPane.setRight(rightVBox);
      }

      {
        GridPane bottomGridPane = new GridPane();
        BackgroundFill backgroundFill = new BackgroundFill(Color.GAINSBORO, CornerRadii.EMPTY, Insets.EMPTY);
        Background background = new Background(backgroundFill);
        bottomGridPane.setBackground(background);
        {
          VBox motorDirectionKeyValueVBox = new VBox();
          {
            Text nameText = new Text("Motor");
            motorDirectionKeyValueVBox.getChildren().add(nameText);
          }
          {
            GridPane keyValueGridPane = new GridPane();
            keyValueGridPane.setPrefSize(120.00, 40.00);
            if (DEBUG) keyValueGridPane.setGridLinesVisible(true);
            {
              Label keyLabel = new Label("direction");
              keyValueGridPane.add(keyLabel, 0, 1);
            }
            {
              Label colonLabel = new Label(":");
              keyValueGridPane.add(colonLabel, 1, 1);
            }
            {
              Label valueLabel = new Label("<int>");
              keyValueGridPane.add(valueLabel, 2, 1);
              updateables.add(new LabeledUpdateable(valueLabel)
              {
                @Override
                protected String updateValue()
                {
                  int value = model.getIn().getMotor().getDirection();
                  return String.valueOf(value);
                }
              });
            }
            motorDirectionKeyValueVBox.getChildren().add(keyValueGridPane);
          }
          bottomGridPane.add(motorDirectionKeyValueVBox, 0, 0);
        }
        {
          VBox motorSpeedKeyValueVBox = new VBox();
          {
            Text nameText = new Text("Motor");
            motorSpeedKeyValueVBox.getChildren().add(nameText);
          }
          {
            GridPane keyValueGridPane = new GridPane();
            keyValueGridPane.setPrefSize(120.00, 40.00);
            if (DEBUG) keyValueGridPane.setGridLinesVisible(true);
            {
              Label keyLabel = new Label("speed");
              keyValueGridPane.add(keyLabel, 0, 1);
            }
            {
              Label colonLabel = new Label(":");
              keyValueGridPane.add(colonLabel, 1, 1);
            }
            {
              Label valueLabel = new Label("<int>");
              keyValueGridPane.add(valueLabel, 2, 1);
              updateables.add(new LabeledUpdateable(valueLabel)
              {
                @Override
                protected String updateValue()
                {
                  int value = model.getIn().getMotor().getSpeed();
                  return String.valueOf(value);
                }
              });
            }
            motorSpeedKeyValueVBox.getChildren().add(keyValueGridPane);
          }
          bottomGridPane.add(motorSpeedKeyValueVBox, 1, 0);
        }
        {
          VBox motorDistanceKeyValueVBox = new VBox();
          {
            Text nameText = new Text("Motor");
            motorDistanceKeyValueVBox.getChildren().add(nameText);
          }
          {
            GridPane keyValueGridPane = new GridPane();
            keyValueGridPane.setPrefSize(120.00, 40.00);
            if (DEBUG) keyValueGridPane.setGridLinesVisible(true);
            {
              Label keyLabel = new Label("distance");
              keyValueGridPane.add(keyLabel, 0, 1);
            }
            {
              Label colonLabel = new Label(":");
              keyValueGridPane.add(colonLabel, 1, 1);
            }
            {
              Label valueLabel = new Label("<int>");
              keyValueGridPane.add(valueLabel, 2, 1);
              updateables.add(new LabeledUpdateable(valueLabel)
              {
                @Override
                protected String updateValue()
                {
                  int value = model.getIn().getMotor().getDistance();
                  return String.valueOf(value);
                }
              });
            }
            motorDistanceKeyValueVBox.getChildren().add(keyValueGridPane);
          }
          bottomGridPane.add(motorDistanceKeyValueVBox, 2, 0);
        }
        {
          VBox motorCounterKeyValueVBox = new VBox();
          {
            Text nameText = new Text("Motor");
            motorCounterKeyValueVBox.getChildren().add(nameText);
          }
          {
            GridPane keyValueGridPane = new GridPane();
            keyValueGridPane.setPrefSize(120.00, 40.00);
            if (DEBUG) keyValueGridPane.setGridLinesVisible(true);
            {
              Label keyLabel = new Label("counter");
              keyValueGridPane.add(keyLabel, 0, 1);
            }
            {
              Label colonLabel = new Label(":");
              keyValueGridPane.add(colonLabel, 1, 1);
            }
            {
              Label valueLabel = new Label("<int>");
              keyValueGridPane.add(valueLabel, 2, 1);
              updateables.add(new LabeledUpdateable(valueLabel)
              {
                @Override
                protected String updateValue()
                {
                  int value = model.getIn().getMotor().getCounter();
                  return String.valueOf(value);
                }
              });
            }
            motorCounterKeyValueVBox.getChildren().add(keyValueGridPane);
          }
          bottomGridPane.add(motorCounterKeyValueVBox, 3, 0);
        }
        {
          VBox lightBarrier1KeyValueVBox = new VBox();
          {
            Text nameText = new Text("Light Barrier 1");
            lightBarrier1KeyValueVBox.getChildren().add(nameText);
          }
          {
            GridPane keyValueGridPane = new GridPane();
            keyValueGridPane.setPrefSize(120.00, 40.00);
            if (DEBUG) keyValueGridPane.setGridLinesVisible(true);
            {
              Label keyLabel = new Label("isIntercepted");
              keyValueGridPane.add(keyLabel, 0, 1);
            }
            {
              Label colonLabel = new Label(":");
              keyValueGridPane.add(colonLabel, 1, 1);
            }
            {
              Label valueLabel = new Label("<boolean>");
              keyValueGridPane.add(valueLabel, 2, 1);
              updateables.add(new LabeledUpdateable(valueLabel)
              {
                @Override
                protected String updateValue()
                {
                  boolean value = model.getIn().getLightBarriers().get(LightBarrierId.COLORSENSOR).isIntercepted();
                  return String.valueOf(value);
                }
              });
            }
            lightBarrier1KeyValueVBox.getChildren().add(keyValueGridPane);
          }
          bottomGridPane.add(lightBarrier1KeyValueVBox, 4, 0);
        }
        {
          VBox lightBarrier2KeyValueVBox = new VBox();
          {
            Text nameText = new Text("Light Barrier 2");
            lightBarrier2KeyValueVBox.getChildren().add(nameText);
          }
          {
            GridPane keyValueGridPane = new GridPane();
            keyValueGridPane.setPrefSize(120.00, 40.00);
            if (DEBUG) keyValueGridPane.setGridLinesVisible(true);
            {
              Label keyLabel = new Label("isIntercepted");
              keyValueGridPane.add(keyLabel, 0, 1);
            }
            {
              Label colonLabel = new Label(":");
              keyValueGridPane.add(colonLabel, 1, 1);
            }
            {
              Label valueLabel = new Label("<boolean>");
              keyValueGridPane.add(valueLabel, 2, 1);
              updateables.add(new LabeledUpdateable(valueLabel)
              {
                @Override
                protected String updateValue()
                {
                  boolean value = model.getIn().getLightBarriers().get(LightBarrierId.EJECTION).isIntercepted();
                  return String.valueOf(value);
                }
              });
            }
            lightBarrier2KeyValueVBox.getChildren().add(keyValueGridPane);
          }
          bottomGridPane.add(lightBarrier2KeyValueVBox, 5, 0);
        }
        bottomGridPane.setPrefSize(800.00, 40.00);
        if (DEBUG) bottomGridPane.setGridLinesVisible(true);

        borderPane.setBottom(bottomGridPane);
      }

      stage.setScene(scene);
    }
    Updater updater = new Updater(updateables);
    stage.show();
    updater.start();
  }

}
