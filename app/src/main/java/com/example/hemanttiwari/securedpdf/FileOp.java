package com.example.hemanttiwari.securedpdf;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;


import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by hemanttiwari on 3/4/16.
 */
public class FileOp {

    private static byte[] KEY;
    private static String TAG = "Hemant"+FileOp.class.getSimpleName();

    private void keyGenerator(String filename)
    {
        try {
            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
            sr.setSeed(filename.getBytes());
            kgen.init(128, sr); // 192 and 256 bits may not be available
            SecretKey skey = kgen.generateKey();
            KEY = skey.getEncoded();


        } catch (Exception e)
        {

            Log.e(TAG, e.toString());
        }

    }


    public  void doEncrypt(Activity ctext, String filename)
    {

        keyGenerator(filename);
        SharedPreferences sharedPref = ctext.getPreferences(Context.MODE_PRIVATE);
        Log.d(TAG, "Encrypting with KEY : " + KEY);

        FileCrypt(ctext, filename, true);
    }

    public void doDecrypt (Activity ctext, String filename)
    {

        Log.d(TAG, "Decrypting with KEY : " +KEY);
        FileCrypt(ctext, filename, false);

    }

    private void FileCrypt(Context ctext, String fileNameOriginal, boolean isEncrypt)
    {
        AssetManager assetManager = ctext.getAssets();
        InputStream in = null;
        FileOutputStream out = null;
        String fileNameEncrypted = "encr"+fileNameOriginal;
        String fileNameDecrypted = "decr"+fileNameOriginal;

        String fileRead = isEncrypt ? fileNameOriginal :fileNameEncrypted;
        String fileWrite = isEncrypt ? fileNameEncrypted:fileNameDecrypted;



        Log.d(TAG, "Reading from : " +fileRead + " , Writing to : " +fileWrite);
        //change to get  encryted file in external dir

        File file = new File(ctext.getFilesDir(), fileWrite);




        try
        {
            if(MainActivity.cryptEnabled) {
                in = ctext.openFileInput(fileRead);
            }else
            {
                in = assetManager.open(fileRead);
            }
            out = ctext.openFileOutput(file.getName(), Context.MODE_PRIVATE);

            doCrypto(in, out, isEncrypt);
            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;
            Log.d(TAG, "File Written to ");
        } catch (Exception e)
        {
            Log.e(TAG, e.getMessage());
        }

    }

    private void doCrypto(InputStream in, OutputStream out, boolean isEncrypt)
    {

        try {
            byte[] inputBuffer = new byte[1024];
            SecretKeySpec skeySpec = new SecretKeySpec(KEY, "AES");
            Cipher cipher = Cipher.getInstance("AES");
            int mode = isEncrypt ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE;
            cipher.init(mode, skeySpec);

            int r = in.read(inputBuffer);
            while (r >= 0) {
                byte[] outputUpdate = cipher.update(inputBuffer, 0, r);
                out.write(outputUpdate);
                r = in.read(inputBuffer);
            }
            byte[] outputFinalUpdate = cipher.doFinal();
            out.write(outputFinalUpdate);

        } catch (Exception e)
        {
            Log.e(TAG,e.toString());
        }
    }


}
