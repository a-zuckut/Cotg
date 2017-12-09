package gui.helper;

// Supports the representation of a statistical pie chart 	
	
import java.awt.*;	
	
public class PieChart {	
	// default characteristics 	
	private static final Color[] DEFAULT_PIE_COLORS = {	
		Color.RED, Color.BLUE, Color.MAGENTA, Color.BLACK,	
		Color.GREEN, Color.ORANGE, Color.CYAN, Color.PINK, 	
		Color.GRAY, Color.YELLOW, Color.DARK_GRAY, Color.WHITE };	
	
	private static final int DEFAULT_RADIUS = 115;	
	private static final int DEFAULT_LINE_SPACING = 20;	
	private static final Point DEFAULT_PIE_LOCATION	
	                               = new Point (10, 10 + 200);	
	private static final Point DEFAULT_LEGEND_LOCATION	
	                               = new Point (265, 35 + 200);	
	
	// individual pie chart characteristics 	
	private double[] data;         // data to be represented 	
	private String[] name;         // legend names for data 	
	private Color[] color;         // colors representing data 	
	private int radius;            // radius of the pie 	
	private Point pieLocation;     // pie location 	
	private Point legendLocation;  // legend location 	
	private int lineSpacing;       // spacing between legend lines 	
	
	// PieChart(): default constructor 	
	public PieChart(double[] d, String[] s) {	
		this(d, s, DEFAULT_PIE_COLORS);	
	}	
	
	// PieChart(): constructor using given data, names, colors 	
	public PieChart(double[] d, String[] s, Color[] c) {	
		if ((d.length == 0) || (d.length > s.length)	
						|| (d.length > c.length)) {	
				System.err.println("PieChart: invalid data");	
				System.exit(1);	
		}	
	
		data = d;	
		name = s;	
		color = c;	
	
		radius = DEFAULT_RADIUS;	
		pieLocation = DEFAULT_PIE_LOCATION;	
		legendLocation = DEFAULT_LEGEND_LOCATION;	
		lineSpacing = DEFAULT_LINE_SPACING;	
	}	
	
	// getSampleSize(): return the number of data values 	
	public int getSampleSize() {	
		return data.length;	
	}	
	
	// getColor(): return the ith color 	
	public Color getColor(int i) {	
		return color[i];	
	}	
	
	// getData(): return the ith data value 	
	public double getData(int i) {	
		return data[i];	
	}	
	
	// getName(): return the ith legend name 	
	public String getName(int i) {	
		return name[i];	
	}	
	
	// getRadius(): return the pie radius 	
	public int getRadius() {	
		return radius;	
	}	
	
	// getPieLocation(): return the location of the pie 	
	public Point getPieLocation() {	
		return pieLocation;	
	}	
	
	// getLegendLocation(): return the location of the legend 	
	public Point getLegendLocation() {	
		return legendLocation;	
	}	
	
	// getLineSpacing(): return space between legend lines	
	public int getLineSpacing() {	
		return lineSpacing;	
	}	
	
	// paint(): render chart and legend 	
	public void paint(Graphics g) {	
		// get pie chart characteristics 	
		int r = getRadius();	
		Point pie = getPieLocation();	
		Point legend = getLegendLocation();	
		int spacing = getLineSpacing();	
	
		// paint the chart 	
		paintPie(g, pie.x, pie.y, r);	
		paintLegend(g, legend.x, legend.y, spacing);	
	}	
	
	// paintPie(): render the pie 	
	public void paintPie(Graphics g, int x, int y, int r) {	
		final int CIRCLE_DEGREES = 360;	
	
		// determine number of values 	
		int n = getSampleSize();	
	
		// compute sum of the values 	
		double dataSum = 0;	
		for (int i = 0; i < n; ++i) {	
			dataSum += getData(i);	
		}	
	
		// draw slices one by one starting from origin 	
		int startAngle = 0;	
	
		for (int i = 0; i < n; ++i) {	
			// how much of the pie does the next slice take	
			double percent = getData(i) / dataSum;	
			int arcAngle = (int) Math.round(percent * CIRCLE_DEGREES);	
	
			// set the slice color	
			Color c = getColor(i);	
			g.setColor(c);	
	
			// render the slice	
			g.fillArc(x, y, 2*r, 2*r, startAngle, arcAngle);	
	
			// record where next slice starts	
			startAngle += arcAngle;	
		}	
	}	
	
	// paintLegend(): render the legend for the pie 	
	public void paintLegend(Graphics g, int x, int y, int delta) {	
		// determine number of values 	
		int n = getSampleSize();	
	
		for (int i = 0; i < n; ++i) {	
			// set up the current legend line	
			Color c = getColor(i);	
			String s = getName(i) + ": " + getData(i);	
	
			// render the line	
			g.setColor(c);	
			g.drawString(s, x, y);	
	
	
			// set up the y-coordinate location of the next line	
			y += delta;	
		}	
	}	
}	
	