/*
 * Copyright 2013 Daniel Baumann
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package at.db.os;

import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.CompoundControl;
import javax.sound.sampled.Control;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;

public class AudioControlUtil {

  // public static void main(String[] args) throws Exception {
  // Mixer.Info[] mixers = AudioSystem.getMixerInfo();
  // System.out.println("There are " + mixers.length + " mixer info objects");
  // for (Mixer.Info mixerInfo : mixers) {
  // System.out.println("mixer name: " + mixerInfo.getName());
  // Mixer mixer = AudioSystem.getMixer(mixerInfo);
  // Line.Info[] lineInfos = mixer.getTargetLineInfo();
  // for (Line.Info lineInfo : lineInfos) {
  // System.out.println("  Line.Info: " + lineInfo);
  // Line line = null;
  // boolean opened = true;
  // try {
  // line = mixer.getLine(lineInfo);
  // opened = line.isOpen() || line instanceof Clip;
  // if (!opened) {
  // line.open();
  // }
  // List<FloatControl> volumeControls = getVolumeControls(line);
  // for (FloatControl fc : volumeControls) {
  // System.out.println("    Volumen Control Value = " + fc.getValue());
  // // fc.setValue(0.0F);
  // // System.out.println("    NEW Value = " + fc.getValue());
  // }
  // } catch (LineUnavailableException e) {
  // e.printStackTrace();
  // } catch (IllegalArgumentException iaEx) {
  // System.out.println("    " + iaEx);
  // } finally {
  // if (line != null && !opened) {
  // line.close();
  // }
  // }
  // }
  // }
  // }


  public static void adjustMasterVolume(float f) {
    Mixer.Info[] mixers = AudioSystem.getMixerInfo();
    for (Mixer.Info mixerInfo : mixers) {
      Mixer mixer = AudioSystem.getMixer(mixerInfo);
      Line.Info[] lineInfos = mixer.getTargetLineInfo();
      for (Line.Info lineInfo : lineInfos) {
        if (lineInfo.toString().toLowerCase().contains("pcm")) {
          Line line = null;
          boolean opened = true;
          try {
            line = mixer.getLine(lineInfo);
            opened = line.isOpen() || line instanceof Clip;
            if (!opened) {
              line.open();
            }

            List<FloatControl> volumeControls = getVolumeControls(line);
            for (FloatControl floatControl : volumeControls) {
              float newVolume = floatControl.getValue() + f;
              if (newVolume > 1.0) {
                newVolume = 1.0F;
              }
              if (newVolume < 0.0) {
                newVolume = 0.0F;
              }
              floatControl.setValue(newVolume);
            }
          } catch (LineUnavailableException e) {
            e.printStackTrace();
          } catch (IllegalArgumentException iaEx) {
            System.out.println("    " + iaEx);
          } finally {
            if (line != null && !opened) {
              line.close();
            }
          }
        }
      }
    }
  }

  public static Line getMasterLine() {
    Mixer.Info[] mixers = AudioSystem.getMixerInfo();
    for (Mixer.Info mixerInfo : mixers) {
      Mixer mixer = AudioSystem.getMixer(mixerInfo);
      Line.Info[] lineInfos = mixer.getTargetLineInfo();
      for (Line.Info lineInfo : lineInfos) {
        if (lineInfo.toString().toLowerCase().contains("master")) {
          Line line = null;
          boolean opened = true;
          try {
            line = mixer.getLine(lineInfo);
            opened = line.isOpen() || line instanceof Clip;
            if (!opened) {
              line.open();
            }

            List<FloatControl> volumeControls = getVolumeControls(line);
            if (volumeControls.size() > 0) {
              return line;
            }
          } catch (LineUnavailableException e) {
            e.printStackTrace();
          } catch (IllegalArgumentException iaEx) {
            System.out.println("    " + iaEx);
          } finally {
            if (line != null && !opened) {
              line.close();
            }
          }
        }
      }
    }
    return null;
  }

  public static List<FloatControl> getVolumeControls(Line line) {
    List<FloatControl> result = new ArrayList<FloatControl>();
    for (Control c : line.getControls()) {
      result.addAll(getVolumeControls(c));
    }
    return result;
  }

  private static List<FloatControl> getVolumeControls(Control control) {
    List<FloatControl> result = new ArrayList<FloatControl>();
    if (control instanceof CompoundControl) {
      Control[] controls = ((CompoundControl) control).getMemberControls();
      for (Control c : controls) {
        result.addAll(getVolumeControls(c));
      }
    } else if (control instanceof FloatControl) {
      FloatControl fc = (FloatControl) control;
      if (fc.getType() == FloatControl.Type.VOLUME) {
        result.add(fc);
      }
    }
    return result;
  }

  // private static void printControl(Control control, String indent) {
  // System.out.printf("%s%s%n", indent, control);
  // if (control instanceof CompoundControl) {
  // Control[] controls = ((CompoundControl) control).getMemberControls();
  // indent += "  ";
  // for (Control c : controls) {
  // printControl(c, indent);
  // }
  // }
  // }

}
