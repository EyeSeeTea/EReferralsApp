/*
 * Copyright (c) 2015.
 *
 * This file is part of QIS Surveillance App.
 *
 *  QIS Surveillance App is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  QIS Surveillance App is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with QIS Surveillance App.  If not, see <http://www.gnu.org/licenses/>.
 */


package org.eyeseetea.malariacare.phonemetadata;

/**
 * Created by ignac on 12/10/2015.
 */
public class PhoneMetaData {
    private String imei;
    private String phone_number;
    private String phone_serial;
    private String build_number;

    public PhoneMetaData() {

    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public String getPhone_serial() {
        return phone_serial;
    }

    public void setPhone_serial(String phone_serial) {
        this.phone_serial = phone_serial;
    }

    public String getBuild_number() {
        return build_number;
    }

    public void setBuild_number(String build_number) {
        this.build_number = build_number;
    }

    public String getPhone_metaData() {
        String phonemetadata = "";
        phonemetadata = "###";
        if (phone_number != null && !phone_number.equals("") && phone_number.length() > 0) {
            phonemetadata = phonemetadata + phone_number;
        }

        phonemetadata = phonemetadata + "###";
        if (imei != null && !imei.equals("") && imei.length() > 0) {
            phonemetadata = phonemetadata + imei;
        }

        phonemetadata = phonemetadata + "###";
        if (phone_serial != null && !phone_serial.equals("") && phone_serial.length() > 0) {
            phonemetadata = phonemetadata + phone_serial;
        }

        phonemetadata = phonemetadata + "###";
        if (build_number != null && !build_number.equals("") && build_number.length() > 0) {
            phonemetadata = phonemetadata + build_number;
        }

        return phonemetadata;
    }
}
