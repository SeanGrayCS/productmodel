package com.seangraycs;

import org.jzy3d.colors.Color;
import org.jzy3d.colors.colormaps.AbstractColorMap;
import org.jzy3d.colors.colormaps.IColorMap;

public class ColorMapAlpha extends AbstractColorMap {
  private IColorMap internalColorMap;
  private float alpha;

  public ColorMapAlpha(IColorMap colorMap) {
    this(colorMap, 1.0f);
  }

  public ColorMapAlpha(IColorMap colorMap, float alpha) {
    super();
    this.internalColorMap = colorMap;
    this.alpha = alpha;
  }

  public void setAlpha(float alpha) {
    this.alpha = alpha;
  }

  @Override
  public Color getColor(double x, double y, double z, double zMin, double zMax) {
    return internalColorMap.getColor(x, y, z, zMin, zMax).alphaSelf(alpha);
  }
}
