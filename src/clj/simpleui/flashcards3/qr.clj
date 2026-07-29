(ns simpleui.flashcards3.qr
  (:require
    [simpleui.flashcards3.env :refer [host]])
  (:import
    [com.google.zxing BarcodeFormat EncodeHintType]
    [com.google.zxing.qrcode QRCodeWriter]
    [com.google.zxing.client.j2se MatrixToImageWriter]
    [java.io ByteArrayOutputStream ByteArrayInputStream]
    [java.util HashMap]))

(defn generate-qr!
  [a key]
  (let [url (host a key)
        hints (doto (HashMap.)
                    (.put EncodeHintType/MARGIN 1))
        matrix (.encode (QRCodeWriter.)
                        url
                        BarcodeFormat/QR_CODE
                        256
                        256
                        hints)
        baos (ByteArrayOutputStream.)]
    (MatrixToImageWriter/writeToStream matrix "PNG" baos)
    (ByteArrayInputStream. (.toByteArray baos))))
