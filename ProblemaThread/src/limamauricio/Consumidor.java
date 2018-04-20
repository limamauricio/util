package limamauricio;

public class Consumidor extends Thread {

	Deposito dep;
	int sleep_time;

	public Consumidor(Deposito dep, int sleep) {
		// TODO Auto-generated constructor stub

		this.dep = dep;
		this.sleep_time = sleep;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
			int i;
			for (i = 0; i < 20; i++) {
				
				while (!dep.retirar()) {
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
					try {
						Thread.sleep(sleep_time);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				System.out.println("Consumidor acabou de consumir.\n");
				System.out.println("Estoque restante:"+ dep.getNumItens());
			}
	
	}

}
