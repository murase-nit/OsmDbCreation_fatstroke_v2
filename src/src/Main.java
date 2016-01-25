package src;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;

import src.handledb.createDb.CreateDb;
import src.handledb.createDb.HandleDbFacility;
import src.handledb.createDb.Looproad;
import src.handledb.createDb.Stroke;
import src.handledb.createDb.StrokeFacilityTable;

public class Main {
	
	/** 右上の緯度経度 */
	public static Point2D _upperLeftLngLat = new Point2D.Double(136.81080420304443, 35.25605501753201);
	/** 左下の緯度経度 */
	public static Point2D _lowerRightLngLat = new Point2D.Double(137.0511301307788, 35.05952383578407);
	
	// テスト用.
//	public static Point2D _upperLeftLngLat = new Point2D.Double(137.00080420304443, 35.06605501753201);
//	public static Point2D _lowerRightLngLat = new Point2D.Double(137.0511301307788, 35.05952383578407);
	
	public static final ArrayList<ArrayList<String>> categoryAll = new ArrayList<>();
	static {
		categoryAll.add(new ArrayList<>(Arrays.asList("Amenity","cafe")));
		categoryAll.add(new ArrayList<>(Arrays.asList("Amenity","fast_food")));
		categoryAll.add(new ArrayList<>(Arrays.asList("Amenity","pub")));
		categoryAll.add(new ArrayList<>(Arrays.asList("Amenity","restaurant")));
		categoryAll.add(new ArrayList<>(Arrays.asList("Amenity","parking")));
		categoryAll.add(new ArrayList<>(Arrays.asList("Amenity","hospital")));
		categoryAll.add(new ArrayList<>(Arrays.asList("Amenity","vending_machine")));
		
		categoryAll.add(new ArrayList<>(Arrays.asList("shop","convenience")));
		categoryAll.add(new ArrayList<>(Arrays.asList("shop","clothes")));
		categoryAll.add(new ArrayList<>(Arrays.asList("shop","supermarket")));
		categoryAll.add(new ArrayList<>(Arrays.asList("shop","book")));
		categoryAll.add(new ArrayList<>(Arrays.asList("highway","traffic_signals")));
	}
	
	public static final ArrayList<String> categoryList = new ArrayList<>(Arrays.asList(
			"pub", "parking", "hospital", "traffic_signals", "fast_food", "restaurant", "vending_machine", "clothes", "supermarket", "cafe", "convenience"));

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		CreateDb createDb = new CreateDb();
		createDb.startConnection();
		createDb.createTable();	// 各種テーブルの作成.
		
		HandleDbFacility handleDbFacility = new HandleDbFacility();
		Looproad looproad = new Looproad();
		Stroke stroke = new Stroke();
		StrokeFacilityTable strokeFacilityTable = new StrokeFacilityTable();
		// step1: ストロークDBと周回道路DBを使ってストローク・周回道路関係テーブルを作成する.
		// step2: 周回道路と施設データを使って周回道路・施設データ関係テーブルを作成する.
		// step3: ストローク・周回道路関係テーブル，周回道路・施設データ関係テーブルを使ってストローク施設データ関係テーブルを作成する.
		// step4: ストローク施設データ関係テーブルからfatstrokeを作成する.
		
		
		// step1.
		// 1-1:指定範囲のストロークを取り出す.
		// 1-2:各ストロークと接する周回道路を取り出す.
		// 1-3:ストロークと周回道路の関係をテーブルに格納.
		looproad = new Looproad();
		stroke = new Stroke();
		looproad.startConnection();
		stroke.startConnection();
		
		stroke.getStrokeFromMBR(_upperLeftLngLat, _lowerRightLngLat);	//1-1
		for(int i=0; i<stroke._strokeArcString.size(); i++){
			looproad.calcNeighberLooproadFromTmpTableUsingStroke(stroke._strokeArcString.get(i));	//1-2
			for(int j=0; j<looproad.areaIdArrayList.size(); j++){
			createDb.insertStrokeAndLooproadTable(stroke._strokeId.get(i), looproad.areaIdArrayList.get(j));	//1-3
			}
		}
		
		looproad.endConnection();
		stroke.endConnection();
		
		// step2.
		// 2-1:指定範囲内の周回道路を取り出す.
		// 2-2:各周回道路内にある施設データを求める(カテゴリ別).
		// 2-3:周回道路と施設データの関係をテーブルに格納.
		looproad = new Looproad();
		handleDbFacility = new HandleDbFacility();
		looproad.startConnection();
		handleDbFacility.startConnection();
		
		looproad.getLooproadFromMBR(_upperLeftLngLat, _lowerRightLngLat);	// 2-1.
		for(int i=0; i<looproad._looproadStrings.size(); i++){
			handleDbFacility.getFacilityFromPolygon(looproad._looproadStrings.get(i));	// 2-2.
			for(int j=0; j<handleDbFacility._id.size(); j++){
				createDb.insertLooproadAndFacilityTable(looproad._areaId.get(i), handleDbFacility._id.get(j), handleDbFacility._category.get(j));	// 2-3.
			}
		}
		
		looproad.endConnection();
		handleDbFacility.endConnection();
		
		// step3.
		// step1,step2のテーブルを用いて各ストロークに接する周回道路内にある施設データを求める(カテゴリ別).
		// 3-1:step1で作成したテーブルからストロークIDを取り出す.
		// 3-2:各ストロークIDと紐づく施設IDを求める.
		// 3-3:ストロークIDと施設IDの関係をテーブルに格納.
		strokeFacilityTable = new StrokeFacilityTable();
		strokeFacilityTable.startConnection();
		strokeFacilityTable.strokeFacility();	// 3-1,3-2.
		for(int j=0; j<strokeFacilityTable._strokeId.size(); j++){
			createDb.insertStrokeAndFacilityTable(strokeFacilityTable._strokeId.get(j), strokeFacilityTable._facilityId.get(j), strokeFacilityTable._category.get(j));	// 3-3.
		}
		strokeFacilityTable.endConnection();
		
		
		// step4.
		// ストロークのID,長さ，施設数，施設カテゴリ，ジオメトリデータをfatstrokeテーブルに格納する.
		// 4-1:step3で作成したテーブルからストロークとカテゴリ別にストロークに紐づく施設数を調べる.
		// 4-2:
		strokeFacilityTable = new StrokeFacilityTable();
		Stroke stroke__ = new Stroke();
		strokeFacilityTable.startConnection();
		stroke__.startConnection();
		System.out.println("stroke id  "+stroke._strokeId.size());
		System.out.println("category list  "+categoryList.size());
		for(int i=0; i<stroke._strokeId.size(); i++){
			for(int j=0; j<categoryList.size(); j++){
				// ストロークに関するデータ.
				stroke__.getStrokeFromId(stroke._strokeId.get(i));
				// 指定したカテゴリのお店がストロークにいくつ紐づいているか.
				int strokeCategoryNum = strokeFacilityTable.coutnStrokeFacilityNum(stroke._strokeId.get(i), categoryList.get(j));
				createDb.insertFatStrokeData(
						stroke._strokeId.get(i), 
						stroke__._oneStrokeLength, 
						strokeCategoryNum, 
						categoryList.get(j),
						stroke__._oneStrokeWktString);
			}
		}
		strokeFacilityTable.endConnection();
		stroke__.endConnection();
		
		System.out.println("finish");
	}

}
