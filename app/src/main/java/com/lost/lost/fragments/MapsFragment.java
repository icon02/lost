package com.lost.lost.fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Switch;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lost.lost.R;
import com.lost.lost.javaRes.friend.Friend;
import com.lost.lost.javaRes.friend.ViewHolder;

import java.util.ArrayList;


public class MapsFragment extends FragmentPassObject implements OnMapReadyCallback {

    GoogleMap map;
    ArrayList<MarkerOptions> marker;

    private String uID = FirebaseAuth.getInstance().getUid();

    private DatabaseReference database = FirebaseDatabase.getInstance().getReference().child("Users/");
    private DatabaseReference myRef = database.child(uID).child("Location/");

    private double lat, lng;

    private FriendsFragment ff = new FriendsFragment();

    private String friendID, friendName;

    private double fLat, fLng;

    private FirebaseRecyclerAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_maps, container, false);
        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.map1);
        mapFragment.getMapAsync(this);
        myRef.keepSynced(true);

        FirebaseRecyclerOptions<Friend> options = new FirebaseRecyclerOptions.Builder<Friend>()
                .setQuery(myRef, Friend.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<Friend, ViewHolder>(options) {
            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_friends, viewGroup, false);

                return new ViewHolder(v);
            }

            @Override
            protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull Friend model) {
                Switch aSwitch = holder.getaSwitch();

                ff.addFriend(model);
            }
        };

        return v;
    }

    @Override
    public void onStart(){
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop(){
        super.onStop();
        adapter.stopListening();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        // Add a marker in Sydney and move the camera TODO
        LatLng mySelf = getPosition();
        
        if(ContextCompat.checkSelfPermission(this.getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
           //GPS permission granted
            map.setMyLocationEnabled(true);
        } else {
            //GPS permission denied

        }

        //map.setMyLocationEnabled(true);
        //map.addMarker(new MarkerOptions().position(mySelf).title("my pos"));
        /*map.addMarker(new MarkerOptions().position(mySelf).title("My Position"));
        map.moveCamera(CameraUpdateFactory.newLatLng(mySelf));
        try {
            //Thread.sleep(3000);
        } catch(Exception e) {}
*/
        for(Friend f : ff.getFriendsList()){
            friendName = f.getName();
            friendID = f.getUserID();

            if (f.isEnabled()){
                map.addMarker(new MarkerOptions()
                        .position(getFriendsPosition(friendID))
                        .title(friendName)
                        .icon(BitmapDescriptorFactory
                        .defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
            }
        }

        //map.animateCamera(CameraUpdateFactory.zoomTo(16f));

    }

    private LatLng getPosition(){

        myRef.child("latitude").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    fLat = dataSnapshot.getValue(Double.class);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        myRef.child("longitude").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    fLng = dataSnapshot.getValue(Double.class);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        LatLng latLng = new LatLng(fLat, fLng);
        return latLng;
    }

    private LatLng getFriendsPosition(String id){
       DatabaseReference friendsRef = database.child(id).child("Location/");

       friendsRef.child("latitude").addValueEventListener(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               if (dataSnapshot != null){
                   lat = dataSnapshot.getValue(Double.class);
               }
           }

           @Override
           public void onCancelled(@NonNull DatabaseError databaseError) {

           }
       });

       friendsRef.child("longitude").addValueEventListener(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               if (dataSnapshot != null){
                   lng = dataSnapshot.getValue(Double.class);
               }
           }

           @Override
           public void onCancelled(@NonNull DatabaseError databaseError) {

           }
       });

       LatLng latLng = new LatLng(lat, lng);
       return latLng;
    }

    /*
    public synchronized void addMarker(LatLng pos, String name) {
        MarkerOptions m = new MarkerOptions().position(pos).title(name);
        marker.add(m);

    } */

    public synchronized void removeMarker(String name) {
        for(MarkerOptions m : marker) {
            if(m.getTitle().equals(name)) marker.remove(m);
        }


    }

    public void refreshMarkers() {
        for(MarkerOptions m : marker) {
            map.addMarker(m);
        }
    }

}
