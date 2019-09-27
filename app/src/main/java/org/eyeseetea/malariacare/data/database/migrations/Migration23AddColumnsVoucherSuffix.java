package org.eyeseetea.malariacare.data.database.migrations;


import com.raizlabs.android.dbflow.annotation.Migration;
import com.raizlabs.android.dbflow.sql.SQLiteType;
import com.raizlabs.android.dbflow.sql.migration.AlterTableMigration;

import org.eyeseetea.malariacare.data.database.AppDatabase;
import org.eyeseetea.malariacare.data.database.model.CountryVersionDB;
import org.eyeseetea.malariacare.data.database.model.QuestionDB;

@Migration(version = 23, database = AppDatabase.class)
public class Migration23AddColumnsVoucherSuffix extends AlterTableMigration<QuestionDB> {

    public Migration23AddColumnsVoucherSuffix(Class<QuestionDB> table) {
        super(table);

        addColumn(SQLiteType.TEXT, "voucher_suffix");
        addColumn(SQLiteType.TEXT, "voucher_suffix_value_condition");
    }

}
