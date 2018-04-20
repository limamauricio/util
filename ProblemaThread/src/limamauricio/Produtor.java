package limamauricio;

public class Produtor extends Thread{

	private Deposito dep;
	private int sleep_time;
	private Thread pro_therad;
	
	public Produtor(Deposito dep, int sleep) {
		// TODO Auto-generated constructor stub
		
		this.dep = dep;
		this.sleep_time = sleep;
		this.pro_therad = null;
		
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		int i;
		for (i = 0; i < 100; i++) {
			dep.colocar();
			try {
				Thread.sleep(sleep_time);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
			
			System.out.println("Produtor parou de produzir\n");
			System.out.println("Estoque Total:"+ dep.getNumItens());
			System.out.println("======================================");
		}
		
	}

