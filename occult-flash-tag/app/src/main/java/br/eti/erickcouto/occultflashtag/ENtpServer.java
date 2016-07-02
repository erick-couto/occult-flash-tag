package br.eti.erickcouto.occultflashtag;
import java.io.Serializable;

public enum ENtpServer implements Serializable {

	ARG01 ("ar.pool.ntp.org"),
	AUS01 ("au.pool.ntp.org"),
	ONBR1 ("pool.ntp.br"),
	CAN01 ("ca.pool.ntp.org"),
	CHI01 ("cl.pool.ntp.org"),
	CHN01 ("cn.pool.ntp.org"),
	FRA01 ("fr.pool.ntp.org"),
    GER01 ("de.pool.ntp.org"),
	IND01 ("in.pool.ntp.org"),
	ITA01 ("it.pool.ntp.org"),
	JAP01 ("jp.pool.ntp.org"),
	MEX01 ("mx.pool.ntp.org"),
	AFR01 ("za.pool.ntp.org"),
	SPA01 ("es.pool.ntp.org"),										        		
	USA01 ("us.pool.ntp.org");
	
	private String server;
	
	private ENtpServer(String server) {
		this.server = server;
	}

	public int getCodigo() {
		return this.ordinal();
	}

	public String getServer() {
		return this.server;
	}
	
    public static ENtpServer lookup(String id) {
        for(ENtpServer en: values()){
           if(en.server.toString().equalsIgnoreCase(id)) return en;
        }  
        return null;
    }

	public static ENtpServer getServerByCode(String code){

		ENtpServer server = null;
		
		if("USA01".equals(code)){
			server = ENtpServer.USA01;
		}else if ("ARG01".equals(code)){
			server = ENtpServer.ARG01;			
		}else if ("AUS01".equals(code)){
			server = ENtpServer.AUS01;
		}else if ("ONBR1".equals(code)){
			server = ENtpServer.ONBR1;
		}else if ("CAN01".equals(code)){
			server = ENtpServer.CAN01;
		}else if ("CHI01".equals(code)){
			server = ENtpServer.CHI01;
		}else if ("CHN01".equals(code)){
			server = ENtpServer.CHN01;
		}else if ("FRA01".equals(code)){
			server = ENtpServer.FRA01;
		}else if ("GER01".equals(code)){
			server = ENtpServer.GER01;
		}else if ("IND01".equals(code)){
			server = ENtpServer.IND01;
		}else if ("ITA01".equals(code)){
			server = ENtpServer.ITA01;
		}else if ("JAP01".equals(code)){
			server = ENtpServer.JAP01;
		}else if ("MEX01".equals(code)){
			server = ENtpServer.MEX01;
		}else if ("AFR01".equals(code)){
			server = ENtpServer.AFR01;
		}else if ("SPA01".equals(code)){
			server = ENtpServer.SPA01;
		}

		return server;
	}

    
}