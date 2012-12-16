/**
 * <p>
 * プロパティの設定・取得を行うクラスを提供する。
 * </p>
 * <h3>基本的な使い方</h3>
 * <p>
 * プロパティの設定・取得を行いたいクラスの public メソッドに、{@link Property}
 * アノテーションをつける。例を以下に記す。アノテーションの詳細は、{@link Property}
 * のドキュメンテーションを参照。
 * <pre>{@code
    public class Clazz {
        @Property(alias = "prop1", mode = Mode.SET)
        public void setAaa(String arg) {
        	this.aaa = arg;
        }
        
        @Property(alias = "prop1", mode = Mode.GET)
        public String setAaa() {
        	return this.aaa;
        }
    }
 * }</pre>
 * </p>
 * <p>
 * Property アノテーションを設定したメソッドへの値の設定、取得は、
 * プロパティリソースごとのユーティリティクラスを使用する。
 * このパッケージでは、プロパティファイル（テキストファイル）をプロパティリソース
 * として使用する、{@link PropertyFileLoader} と {@link PropertyFileStorer}
 * クラスが提供される。例を以下に記す。
 * <pre>{@code
    // プロパティファイルから読んだ値をオブジェクトへ設定
    Clazz obj = new Clazz();
    PropertyFileLoader loader = 
        new PropertyFileLoader(new File("test.properties"));
    loader.load(obj);
    
    // オブジェクトの値をプロパティファイルに書き出し
    PropertyFileStorer storer =
        new PropertyFileStorer(new File("test.properties"));
    storer.store(obj);
 * }</pre>
 * </p>
 */
package net.mikaboshi.property;
