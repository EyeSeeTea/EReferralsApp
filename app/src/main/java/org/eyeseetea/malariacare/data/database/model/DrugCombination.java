package org.eyeseetea.malariacare.data.database.model;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.eyeseetea.malariacare.data.database.AppDatabase;

import java.util.List;

@Table(database = AppDatabase.class)
public class DrugCombination extends BaseModel {
    @Column
    @PrimaryKey(autoincrement = true)
    long id_drug_combination;
    @Column
    Long id_drug_fk;
    @Column
    Long id_treatment_fk;

    /**
     * Reference to drug (loaded lazily)
     */
    Drug drug;
    /**
     * Reference to treatment (loaded lazily)
     */
    Treatment treatment;

    public DrugCombination() {
    }

    public DrugCombination(long id_drug_combination, long id_drug, long id_treatment) {
        this.id_drug_combination = id_drug_combination;
        this.id_drug_fk = id_drug;
        this.id_treatment_fk = id_treatment;
    }

    public static List<DrugCombination> getAllDrugCombination() {
        return new Select().from(DrugCombination.class).queryList();
    }

    public long getId_drug_combination() {
        return id_drug_combination;
    }

    public void setId_drug_combination(long id_drug_combination) {
        this.id_drug_combination = id_drug_combination;
    }

    public Drug getDrug() {
        if (drug == null) {
            if (id_drug_fk == null) {
                return null;
            }
            drug = new Select()
                    .from(Drug.class)
                    .where(Drug_Table.id_drug
                            .is(id_drug_fk)).querySingle();
        }
        return drug;
    }

    public void setDrug(Drug drug) {
        this.drug = drug;
        id_drug_fk = (drug != null) ? drug.getId_drug() : null;
    }

    public void setDrug(Long id_drug) {
        this.id_drug_fk = id_drug;
        drug = null;
    }

    public Treatment getTreatment() {
        if (treatment == null) {
            if (id_treatment_fk == null) {
                return null;
            }
            treatment = new Select()
                    .from(Treatment.class)
                    .where(Treatment_Table.id_treatment
                            .is(id_treatment_fk)).querySingle();
        }
        return treatment;
    }

    public void setTreatment(Treatment treatment) {
        this.treatment = treatment;
        id_treatment_fk = (treatment != null) ? treatment.id_treatment : null;
    }

    public void setTreatment(Long id_treatment) {
        this.id_treatment_fk = id_treatment;
        treatment = null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DrugCombination that = (DrugCombination) o;

        if (id_drug_combination != that.id_drug_combination) return false;
        if (id_drug_fk != that.id_drug_fk) return false;
        return id_treatment_fk == that.id_treatment_fk;

    }

    @Override
    public int hashCode() {
        int result = (int) (id_drug_combination ^ (id_drug_combination >>> 32));
        result = 31 * result + (int) (id_drug_fk ^ (id_drug_fk >>> 32));
        result = 31 * result + (int) (id_treatment_fk ^ (id_treatment_fk >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "DrugCombination{" +
                "id_drug_combination=" + id_drug_combination +
                ", id_drug_fk=" + id_drug_fk +
                ", id_treatment_fk=" + id_treatment_fk +
                '}';
    }
}
