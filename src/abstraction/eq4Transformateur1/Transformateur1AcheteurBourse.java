package abstraction.eq4Transformateur1;

import java.util.ArrayList;
import java.util.List;

import abstraction.eqXRomu.bourseCacao.IAcheteurBourse;
import abstraction.eqXRomu.contratsCadres.ExemplaireContratCadre;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.general.Journal;
import abstraction.eqXRomu.produits.ChocolatDeMarque;
import abstraction.eqXRomu.produits.Feve;
/**
 *  @author Bacem_CHTOUROU
 */
public class Transformateur1AcheteurBourse extends Transformateur1Acteur implements IAcheteurBourse {


	protected Journal journalAchatBourse;

	public Transformateur1AcheteurBourse() {
		super();

		this.journalAchatBourse = new Journal(this.getNom()+" journalAchatBourse", this);
	}

	public List<Journal> getJournaux() {
		List<Journal> res=super.getJournaux();
		res.add(journalAchatBourse);
		return res;
	}

	/**
	 * l'idée est de calculer la quantité de fèves à acheter en bourse
	 *  en fonction des besoins pour deux mois de contrats cadres. 
	 * La répartition des stocks est de 30% pour les fèves de haute qualité (HQ) et
	 *  70% pour les fèves de moyenne qualité (MQ)
	 */
	public double demande(Feve f, double cours) {
		double stockCible = Math.max(2*this.demandeCC, 20000);
		stockCible = Math.max(stockCible - this.totalStocksChocoMarque.getValeur(this.cryptogramme), 0);
		this.journalAchatBourse.ajouter("- Le stock cible est de "+stockCible+"T de feve");
		double demandeMin=100;
		double stockCibleHQ = 0.3 * stockCible;
	    double stockCibleMQ = 0.7 * stockCible;
	    if (f == Feve.F_HQ_BE) {
	        if (this.stockFeves.get(f).getValeur() < stockCibleHQ) {
	        	double demandeHQ = Math.max(stockCibleHQ - this.stockFeves.get(f).getValeur(), demandeMin);
	    		this.journalAchatBourse.ajouter("- La demande en HQ_BE est de "+demandeHQ+"T de fève");
	            return demandeHQ;}}
	    else if (f == Feve.F_MQ) {
	        if (this.stockFeves.get(f).getValeur() < stockCibleMQ) {
	        	double demandeMQ = Math.max(stockCibleMQ - this.stockFeves.get(f).getValeur(), demandeMin);
	    		this.journalAchatBourse.ajouter("- La demande en MQ_E est de "+demandeMQ+"T de fève");
	            return demandeMQ;
	        }
	    }
		return 0;
	}

	@Override
	public void notificationAchat(Feve f, double quantiteEnT, double coursEnEuroParT) {

		// TODO Auto-generated method stub
		this.stockFeves.get(f).setValeur(this, this.stockFeves.get(f).getValeur()+quantiteEnT);
		this.totalStocksFeves.ajouter(this, quantiteEnT, cryptogramme);
	
		this.journalAchatBourse.ajouter("- achat de "+quantiteEnT+"T de fèves "+f);


	}
	public void next() {
		super.next();
		this.journalAchatBourse.ajouter("=== STEP "+Filiere.LA_FILIERE.getEtape()+" ====================");
	}

	@Override
	public void notificationBlackList(int dureeEnStep) {
		journalAchatBourse.ajouter(Filiere.LA_FILIERE.getEtape()+" ## BLACKLIST ## pendant "+dureeEnStep+" etapes");
	}
	
}
