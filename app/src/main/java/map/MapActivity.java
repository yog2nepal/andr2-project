package map;

import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.samman.main.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import login.LoginActivity;
import resetpassword.ResetPasswordDialogFragment;
import model.User;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback,GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==PICK_IMAGE_REQUEST&&resultCode==RESULT_OK&&data.getData()!=null) {

            filePath = data.getData();

            if (filePath != null) {
                StorageReference mstorageRef=storageReference.child(firebaseUser.getUid()).child("photo");
                mstorageRef.putFile(filePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                            int height = imgUserPic.getHeight();
                            int width = imgUserPic.getWidth();
                            bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height);
                            imgUserPic.setImageBitmap(bitmap);
                            Toast.makeText(getApplication().getBaseContext(), "Photo uploaded", Toast.LENGTH_LONG).show();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
        else{
            Toast.makeText(this, "did not choose a photo", Toast.LENGTH_LONG).show();
        }
    }

    private static final int PICK_IMAGE_REQUEST = 123;
    private Uri filePath, downloadUri;
    //instance variables
    //for drawer layout
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private FragmentTransaction fragmentTransaction;
    private NavigationView navigationView;

    //showing user info - UI part
    private TextView txtEmail,txtUserName;
    private ImageView imgUserPic;
    private Button btnUpload;
    //for firebase
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private StorageReference storageReference;
    private DatabaseReference databaseReference;
    private String userID;
    private LatLng ll;
    private  List<User> userList;
    private List<User> closeUserList;
    private List<Float> closestDistList;

    //for map
    private SupportMapFragment supportMapFragment;
    private GoogleMap gm;
    private GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    Marker currentUserMarker;
    private List<Marker> addedMarkers;
    LatLng currentUserLatLng;

    //for vibration and notification
    private Vibrator vibrator;

    //my custom method begins
    /**
     * this method is used to check google play service available on the mobile on which
     * user is trying to run the application
     * @return
     */
    private boolean googleServiceAvailable() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int isAvailable = apiAvailability.isGooglePlayServicesAvailable(this);
        if (isAvailable == ConnectionResult.SUCCESS)
            return true;
        else if (apiAvailability.isUserResolvableError(isAvailable)) {
            Dialog dialog = apiAvailability.getErrorDialog(this, isAvailable, 0);
            dialog.show();
        } else {
            Toast.makeText(this, "Cannot connect to play services", Toast.LENGTH_LONG).show();
        }
        return false;
    }

    /**
     * loads a map
     */
    private void initMap() {
        this.supportMapFragment=SupportMapFragment.newInstance();
        this.supportMapFragment.getMapAsync(this);
    }

    /**
     * loads user profile of logged in user
     */
    private void  GetUserPhoto(){
        storageReference.child(firebaseUser.getUid()+"/photo").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                try {
                    Picasso.with(MapActivity.this).load(uri).fit().centerCrop().into(imgUserPic);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
    /**
     * to set the user name in the header view
     */
    private void GetUserName(){
        this.databaseReference.child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                for (DataSnapshot child : children) {
                    if (firebaseUser.getUid().equals(child.getKey())) {
                        Iterable<DataSnapshot> children1 = child.getChildren();
                        for (DataSnapshot c : children1) {

                            if(c.getKey().equals("name"))
                                txtUserName.setText(c.getValue(String.class));
                        }
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
    /**
     * set connection to database
     */
    private  void DatabaseConnection() {
        this.databaseReference.child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<LatLng> temp=new ArrayList<LatLng>();
                userList=new ArrayList<User>();
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                for (DataSnapshot child : children) {
                    if (!firebaseUser.getUid().equals(child.getKey())) {
                        Iterable<DataSnapshot> children1 = child.getChildren();
                        double lat =Double.NaN;
                        double Lng =Double.NaN;
                        String userName=null;
                        for (DataSnapshot c : children1) {

                            if (c.getKey().equals("latitude"))
                                lat = c.getValue(double.class);
                            else if (c.getKey().equals("longitude"))
                                Lng = c.getValue(double.class);
                            else if(c.getKey().equals("name"))
                                userName=c.getValue(String.class);
                        }
                        if (!Double.isNaN(lat) &&!Double.isNaN(Lng)&&userName!=null) {
                            User user=new User();
                            user.setUserID(child.getKey());
                            user.setLatlng(new LatLng(lat,Lng));
                            user.setUserName(userName);
                            userList.add(user);
                            temp.add(new LatLng(lat, Lng));
                            lat =Double.NaN;
                            Lng =Double.NaN;
                        }
                    }
                    else{
                        Iterable<DataSnapshot> children1 = child.getChildren();
                        double lat = Double.NaN;
                        double Lng = Double.NaN;
                        for (DataSnapshot c : children1) {

                            if (c.getKey().equals("latitude"))
                                lat = c.getValue(double.class);
                            else if (c.getKey().equals("longitude"))
                                Lng = c.getValue(double.class);
                        }
                        if (!Double.isNaN(lat) &&!Double.isNaN(Lng)) {
                            currentUserLatLng=new LatLng(lat,Lng);
                            lat = Double.NaN;
                            Lng = Double.NaN;
                        }
                    }
                }
                if(addedMarkers.size()>0)
                {
                    for (Marker m:addedMarkers) {
                        m.remove();
                    }
                    addedMarkers=new ArrayList<Marker>();
                }
                if(temp.size()>0&&currentUserLatLng!=null) {
                    int count=1;
                    closeUserList=new ArrayList<User>();
                    closestDistList=new ArrayList<Float>();
                    addedMarkers=new ArrayList<Marker>();
                    for (User user:userList) {
                        if(currentUserMarker!=null) {
                            float distance=calculateDistance(currentUserMarker.getPosition(),user.getLatlng());
                            String distanceString=String.format("%.02f",distance);
                            if(distance>=0&&distance<=10){
                                user.setDistance(distance);
                                closeUserList.add(user);
                                closestDistList.add(distance);
                            }
                            addedMarkers.add(gm.addMarker(new MarkerOptions().position(user.getLatlng()).visible(true).title(distanceString+" m")));
                            count++;
                        }
                        else {
                            float distance=calculateDistance(currentUserLatLng,user.getLatlng());
                            String distanceString=String.format("%.02f",distance);
                            if(distance>=0&&distance<=10){
                                user.setDistance(distance);
                                closeUserList.add(user);
                                closestDistList.add(distance);
                            }

                            addedMarkers.add(gm.addMarker(new MarkerOptions().position(user.getLatlng()).visible(true).title(distanceString+" m")));

                            count++;
                        }
                    }
                    if(closeUserList.size()>0){
                        vibrator.vibrate(500);
                        int notNumber=1;
                        for (User user:closeUserList){
                            addNotification(user.getUserName(),notNumber);
                            notNumber++;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * calculate the distance between logged user with other users
     * @return
     */
    private float calculateDistance(LatLng currentUserLatLng,LatLng otherUserLatlng){
    float tempDistance=0;
    Location currentUser,otherUser;

        currentUser=new Location("Current User");
        currentUser.setLatitude(currentUserLatLng.latitude);
        currentUser.setLongitude(currentUserLatLng.longitude);

        otherUser=new Location("Other User");
        otherUser.setLongitude(otherUserLatlng.longitude);
        otherUser.setLatitude(otherUserLatlng.latitude);

        tempDistance=currentUser.distanceTo(otherUser);

    return  tempDistance;
}

    private void addNotification(String userName,int notNumber){
        NotificationCompat.Builder builder =new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.drawable.nearby);
        builder.setContentTitle("People near by!");
        builder.setContentText(userName+" is near by you");
      //  Intent intent = new Intent(this, MainActivity.class);
        //PendingIntent pendingIntent= PendingIntent.getActivity(this, 0, intent, 0);
        //builder.setContentIntent(pendingIntent);
        Notification not = builder.build();
        NotificationManager nm = (NotificationManager)getSystemService(Service.NOTIFICATION_SERVICE);
        nm.notify(notNumber, not);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(this.googleServiceAvailable()){
            Toast.makeText(this, "Perfect", Toast.LENGTH_LONG).show();
            setContentView(R.layout.activity_map);

            // Initialize Firebase Auth and Database Reference
            firebaseAuth = FirebaseAuth.getInstance();
            firebaseUser = firebaseAuth.getCurrentUser();
            storageReference= FirebaseStorage.getInstance().getReference();
            databaseReference = FirebaseDatabase.getInstance().getReference();

            //loads a map
            this.initMap();

            //navigation drawer section
            this.toolbar=(Toolbar)findViewById(R.id.toolbar);
            setSupportActionBar(this.toolbar);
            this.drawerLayout=(DrawerLayout)findViewById(R.id.drawer_layout);
            this.actionBarDrawerToggle=new ActionBarDrawerToggle(this,this.drawerLayout,this.toolbar,R.string.drawer_open,R.string.drawer_close);
            this.drawerLayout.addDrawerListener(actionBarDrawerToggle);
            this.fragmentTransaction=getSupportFragmentManager().beginTransaction();
            this.fragmentTransaction.add(R.id.main_container,this.supportMapFragment);
            this.fragmentTransaction.commit();
            getSupportActionBar().setTitle("Your Location");
            this.navigationView=(NavigationView)findViewById(R.id.navigation_view);
            this.navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch(item.getItemId())
                    {
                        case R.id.ownLocation_id:
                            fragmentTransaction=getSupportFragmentManager().beginTransaction();
                            fragmentTransaction.replace(R.id.main_container,supportMapFragment);
                            fragmentTransaction.commit();
                            getSupportActionBar().setTitle("Your location");
                            item.setChecked(true);
                            drawerLayout.closeDrawers();
                            break;
                        case  R.id.changePassword:
                            ResetPasswordDialogFragment dialog=new ResetPasswordDialogFragment();
                            dialog.SetMapActivityContext(getBaseContext());
                            dialog.show(getSupportFragmentManager(),null);
                            break;
                        case R.id.logout:
                            firebaseAuth.signOut();
                            Intent intent = new Intent(MapActivity.this, LoginActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            firebaseAuth=null;
                            firebaseUser=null;
                            Toast.makeText(MapActivity.this, "Logout successful", Toast.LENGTH_LONG).show();
                            item.setChecked(true);
                            drawerLayout.closeDrawers();
                            break;
                    }
                    return false;
                }
            });

            //sets the user information in the navigation drawer
            View headerView=this.navigationView.getHeaderView(0);
            this.txtEmail=(TextView)headerView.findViewById(R.id.txtEmail);
            this.txtUserName=(TextView)headerView.findViewById(R.id.txtUserName);
            this.imgUserPic=(ImageView)headerView.findViewById(R.id.imgUserpic);
            this.btnUpload=(Button)headerView.findViewById(R.id.btnUpdatePic);
            this.btnUpload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent= new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent,"Select an image"),PICK_IMAGE_REQUEST);
                }
            });
            if(this.firebaseUser!=null)
            {
                this.txtEmail.setText(this.firebaseUser.getEmail().trim());
                this.GetUserName();
                this.GetUserPhoto();
            }
            this.currentUserLatLng=null;
            this.currentUserMarker=null;
            this.vibrator=(Vibrator)getSystemService(VIBRATOR_SERVICE);
            this.closestDistList=null;
            this.closeUserList=null;
            this.addedMarkers=new ArrayList<Marker>();

            String temp=getIntent().getClass().toString();
            if(getIntent().getExtras()!=null){
                String name =getIntent().getExtras().get("name").toString();
                if(this.firebaseUser!=null && name!=null) {
                    this.userID = this.firebaseUser.getUid();
                    this.databaseReference.child("users").child(this.userID).child("name").setValue(name);
                }
            }
        }
        else{
            Toast.makeText(this, "Cannot find google service", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        this.actionBarDrawerToggle.syncState();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gm = googleMap;
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();

        //set connection to database and listen for value changes in the database
        this.DatabaseConnection();
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(100);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest,this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        if(location==null){
            Toast.makeText(this, "Cannot find current location", Toast.LENGTH_LONG).show();
        }
        else{

            if(currentUserMarker!=null)
            {
                currentUserMarker.remove();
            }
            if(this.firebaseUser!=null) {
                this.ll=new LatLng(location.getLatitude(),location.getLongitude());
                this.userID=this.firebaseUser.getUid();
                this.databaseReference.child("users").child(this.userID).child("latitude").setValue(this.ll.latitude);
                this.databaseReference.child("users").child(this.userID).child("longitude").setValue(this.ll.longitude);
                currentUserMarker=gm.addMarker(new MarkerOptions().position(ll).visible(true).title("Hello,I am here"));
                //CameraUpdate cameraUpdate= CameraUpdateFactory.newLatLngZoom(ll,20);
               // gm.animateCamera(cameraUpdate);
            }
        }
    }
}
