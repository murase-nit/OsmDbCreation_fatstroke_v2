package src.handledb.createDb;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.sql.ResultSet;
import java.util.ArrayList;


import org.postgis.PGgeometry;

import src.handledb.GeometryParsePostgres;
import src.handledb.HandleDbTemplateSuper;

/**
 * ストロークの処理に関する
 * @author murase
 *
 */
public class Stroke extends HandleDbTemplateSuper{
	protected static final String DBNAME = "osm_road_db";	// Database Name
	protected static final String SCHEMA = "stroke";
	protected static final String TBNAME = "flatted_stroke_table";
	protected static final String USER = "postgres";			// user name for DB.
	protected static final String PASS = "usadasql";		// password for DB.
	protected static final String URL = "rain2.elcom.nitech.ac.jp";
	protected static final int PORT = 5432;
	protected static final String DBURL = "jdbc:postgresql://"+URL+":"+PORT+"/" + DBNAME;
	
	public Stroke() {
		super(DBNAME, USER, PASS, DBURL, HandleDbTemplateSuper.POSTGRESJDBCDRIVER_STRING);
	}
	
	
	/** ストロークID */
	public ArrayList<Integer> _strokeId = new ArrayList<>();
	// 切り出さずにそのままのストローク.
	/** データベースからそのまま取り出したストローク(arc形式) */
	public ArrayList<ArrayList<Line2D>> _strokeArc = new ArrayList<>();
	/** ストロークのWKT形式 */
	public ArrayList<String> _strokeArcString = new ArrayList<>();
	/** ストロークの長さ */
	public ArrayList<Double> _strokeLength = new ArrayList<>();
	
	/**
	 * 指定範囲内のストロークを取り出す
	 */
	public void getStrokeFromMBR(Point2D aUpperLeftLngLat, Point2D aLowerRightLngLat){
		_strokeId = new ArrayList<>();
		_strokeArc = new ArrayList<>();
		_strokeArcString = new ArrayList<>();
		_strokeLength = new ArrayList<>();
		try{
			String statement;
			statement = "select "+
					" id, length, clazz, flatted_arc_series, st_asText(flatted_arc_series) as strokeString"+
					" from "+SCHEMA+"."+TBNAME+" " +
					" where" +
					" st_intersects(" +
						"st_polygonFromText(" +
							"'Polygon(("+
								aUpperLeftLngLat.getX()+" "+aUpperLeftLngLat.getY()+","+
								aLowerRightLngLat.getX()+" "+aUpperLeftLngLat.getY()+","+
								aLowerRightLngLat.getX()+" "+aLowerRightLngLat.getY()+","+
								aUpperLeftLngLat.getX()+" "+aLowerRightLngLat.getY()+","+
								aUpperLeftLngLat.getX()+" "+aUpperLeftLngLat.getY()+
								"))'," +
							""+HandleDbTemplateSuper.WGS84_EPSG_CODE+")," +
						"flatted_arc_series);";
			System.out.println(statement);
			ResultSet rs = execute(statement);
			while(rs.next()){
				_strokeId.add(rs.getInt("id"));
				_strokeArc.add(GeometryParsePostgres.getLineStringMultiLine((PGgeometry)rs.getObject("flatted_arc_series")));
				_strokeArcString.add(rs.getString("strokeString"));
				_strokeLength.add(rs.getDouble("length"));
			}
			rs.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
	}
	
	
	
	public double _oneStrokeLength;
	public String _oneStrokeWktString;
	/**
	 * idからストロークを取り出す
	 */
	public void getStrokeFromId(int aStrokeId){
		try{
			String stmt = "" +
					"select " +
						"id, length, clazz, flatted_arc_series, st_asText(flatted_arc_series) as strokeString "+
					" from "+SCHEMA+"."+TBNAME+" " +
					" where" +
					" id = "+aStrokeId+"";
			ResultSet rs= execute(stmt);
			while(rs.next()){
				_oneStrokeLength = rs.getDouble("length");
				_oneStrokeWktString = rs.getString("strokeString");
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	
	
}
