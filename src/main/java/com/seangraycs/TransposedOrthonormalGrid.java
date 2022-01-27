package com.seangraycs;

import com.seangraycs.TransposedMapper.MappedValue;

import java.util.ArrayList;
import java.util.List;

import org.jzy3d.maths.Coord3d;
import org.jzy3d.maths.Range;
import org.jzy3d.plot3d.builder.Mapper;
import org.jzy3d.plot3d.builder.concrete.OrthonormalGrid;

public class TransposedOrthonormalGrid extends OrthonormalGrid {
  
  private MappedValue mappedValue;
  
  public TransposedOrthonormalGrid(Range xyrange, int xysteps) {
    this(xyrange, xysteps, MappedValue.Z);
  }

  public TransposedOrthonormalGrid(Range xrange, int xsteps, Range yrange, int ysteps) {
    this(xrange, xsteps, yrange, ysteps, MappedValue.Z);
  }

  public TransposedOrthonormalGrid(Range xyrange, int xysteps, MappedValue mappedValue) {
    super(xyrange, xysteps);
    this.mappedValue = mappedValue;
  }

  public TransposedOrthonormalGrid(Range xrange, int xsteps, Range yrange, int ysteps, MappedValue mappedValue) {
    super(xrange, xsteps, yrange, ysteps);
    this.mappedValue = mappedValue;
  }

  @Override
  public List<Coord3d> apply(Mapper mapper) {
    List<Coord3d> output = new ArrayList<Coord3d>(xsteps * ysteps);

    if (!(mapper instanceof TransposedMapper)) {
      return output;
    }
    TransposedMapper tMapper = (TransposedMapper) mapper;
    
    double xstep = xrange.getRange() / (double) (xsteps - 1);
    double ystep = yrange.getRange() / (double) (ysteps - 1);

    double[] aVals = new double[xsteps*ysteps];
    double[] bVals = new double[xsteps*ysteps];
    for (int xi = 0; xi < xsteps; xi++) {
      for (int yi = 0; yi < ysteps; yi++) {
        aVals[xi*ysteps + yi] = xrange.getMin() + xi * xstep;
        bVals[xi*ysteps + yi] = yrange.getMin() + yi * ystep;
      }
    }
    double[] predictions = tMapper.getPredictions(aVals, bVals);
    
    for (int i = 0; i < xsteps*ysteps; i++) {
        double a = aVals[i];
        double b = bVals[i];
        double c = predictions[i];

        double x, y, z;
        switch (mappedValue) {
          case X -> {
            x = c;
            y = a;
            z = b;
          }
          case Y -> {
            x = a;
            y = c;
            z = b;
          }
          case Z -> {
            x = a;
            y = b;
            z = c;
          }
          default -> {
            x = a;
            y = b;
            z = c;
          }
        }

        if (z < 0) {
          z = 0;
        }
        output.add(new Coord3d(x, y, z));
    }
    return output;
  }

}
