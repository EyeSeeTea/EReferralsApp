package org.eyeseetea.malariacare.domain.exception;

public class NoFilesException extends FileDownloadException {
        public static final String ERROR_MESSAGE =
                "Exception info: No exists new files to download";

        public NoFilesException() {
            super(ERROR_MESSAGE);
            System.out.println(ERROR_MESSAGE);
        }
}
