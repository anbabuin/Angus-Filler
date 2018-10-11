package com.company;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;

public class Main extends Thread{

    public static void main(String[] args){

        //String url ="http://localhost:8086/write?db=Angus_v1";
        String old_data="establishments id=05,address=\"some address5\"";
        //posting(url,old_data);
        String sample_meas_data ="mymeas id=0,sensor_id=1,value=17";

        //Random rn = new Random();
        //long example = rn.nextLong(10000.0);
        int i=0;

        //temperature, water(level), moisture, pH, power(electrical), rpm(rotations per minute), uptime
        int temp_bound = 70;    // C° celsius
        int water_bound = 100;  // %
        int moisture_bound = 100; // %
        int pH_bound = 14; //
        int power_bound = 10000; // WattH
        int rpm_bound = 10000; // rpm
        int waterUse_bound = 25; // l/s

        int time_counter = 0;   // seconds


        while(true){

            try{
                Thread.sleep(1000);

                time_counter++;

                // temperature[1,3,5]
                batchInsert(i, new int[] {1,3,5}, temp_bound);

                // water level[2,4,7,9,11]
                batchInsert(i, new int[] {2,4,7,9,11}, water_bound);

                // moisture [6]
                batchInsert(i, new int[] {6}, moisture_bound);

                // pH[8,10,12]
                batchInsert(i, new int[] {8,10,12}, pH_bound);

                // electric power[13,16,19,22,25]
                batchInsert(i, new int[] {13,16,19,22,25}, power_bound);

                // rpm [14,17,20,23,26]
                batchInsert(i, new int[] {14,17,20,23,26}, rpm_bound);

                // uptime [15,18,21,24,27]
                batchInsertTime(i, new int[] {15,18,21,24,27}, time_counter);

                //uptime with simulation of machine failure(sensor 27, machine id=11)
                /*
                batchInsertTime(i, new int[] {15,18,21,24}, time_counter);
                timedBatchInsert(i, new int[] {27}, time_counter, 1);
                */


                // water usage [28,29,30]
                batchInsert(i, new int[] {28,29,30}, waterUse_bound);

                System.out.println("done insertion: "+i);

                i++;


            }catch(InterruptedException e){System.out.println(e);}
        }//while true


    }


    public static void posting(String url, String body_data){
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);
        //String body_data = "establishments id=05,address=\"some address5\"";
        try{
            HttpEntity entity = new ByteArrayEntity(body_data.getBytes("UTF-8"));
            httpPost.setEntity(entity);

            try{
                CloseableHttpResponse response2 = httpclient.execute(httpPost);
                try {
                    System.out.println(response2.getStatusLine());
                    HttpEntity entity2 = response2.getEntity();
                    // do something useful with the response body
                    // and ensure it is fully consumed
                    EntityUtils.consume(entity2);
                } finally {
                    response2.close();
                }
            }catch(IOException ioe){
                ioe.printStackTrace();
            }
        }catch(UnsupportedEncodingException e){
            e.printStackTrace();
        }

    }

    public static void influxInsert(int iteration, int sensorId, int value){
        String url ="http://localhost:8086/write?db=Angus_v1";
        String content = "testdata,tag_id="+iteration+",tag_sensor_id="+sensorId+" id="+iteration+",sensor_id="+sensorId+",value="+value;
        posting(url, content);
    }

    public static void batchInsert(int iteration, int[] sensors, int value){
        Random rn = new Random();
        for(int i = 0; i < sensors.length; i++){
            influxInsert(iteration, sensors[i], rn.nextInt(value));
        }
    }

    public static void batchInsertTime(int iteration, int[] sensors, int value){
        for(int i = 0; i < sensors.length; i++){
            influxInsert(iteration, sensors[i], value);
        }
    }

    //after minutes value, stops adding data. (the while loop sleeps 1sec)
    public static void timedBatchInsert(int iteration, int[] sensors, int value, int minutes){
        int seconds = minutes *60;
        Random rn = new Random();
        for(int i = 0; i < sensors.length; i++){
            if (seconds >= iteration){
                //System.out.println("there are "+seconds+"seconds.");
                //influxInsert(iteration, sensors[i], rn.nextInt(value));
                influxInsert(iteration, sensors[i], value);
                seconds --;
            } //else { System.out.println("ended timed insertion");}
        }
    }

}

