package org.neo4j;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Query;
import org.neo4j.driver.Result;

public class Shell implements AutoCloseable {
	private final Driver driver;

	public Shell(String uri, String user, String password) {
		driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
	}

	@Override
	public void close() throws RuntimeException {
		driver.close();
	}

	public void query(final String queryText, final boolean resultExpected) {
		try (var session = driver.session()) {
			String greeting = session.executeWrite(tx -> {
				Query query = new Query(
						queryText
				);
				Result result = tx.run(query);
				return resultExpected ?
						result.single().get(0).asString()
						: "ok, that went well";
			});
			System.out.println(greeting);
		}
	}

	public static void shell(String cmd, final boolean resultExpected) {
		System.out.println("main( cmd="+ cmd +")");
		try (Shell shell = new Shell("bolt://localhost:7687", "neo4j", "01234567")) {
			shell.query( cmd, resultExpected );
		}
	}
	public static void main(String[] args) {
		if (args.length > 3) {
			String action = args[ 0 ];
			String type   = args[ 1 ];
			String attr   = args[ 2 ];
			
			StringBuilder sb = new StringBuilder();
			for (int i=3; i<args.length; i++)
				sb.append( args[ i ]+" " );
			String value  = sb.toString();
			
			if (action.equals("CREATE"))
				shell("CREATE (a:"+ type +") SET a."+ attr +" = '"+ value +"' RETURN a."+ attr +" + ', from node ' + id(a)", true);

			else if (action.equals("DELETE"))
				shell("MATCH (n:"+ type +" {"+ attr +":'"+ value +"'}) DELETE n", false);
			
			//else if (action.equals( "LINK" ))
			//	shell("CREATE (n:"+ type +" {name: 'Alice'})-[:FRIEND]->(m:Person {name: 'Bob'})", true);

			else
				System.out.println( "Usage: shell CREATE|DELETE <type> <attr> <value>"+ action );
			
		} else
			System.out.println( "Usage: shell CREATE|DELETE <type> <attr> <values...>"+ args[ args.length-1 ] );
	}
}
