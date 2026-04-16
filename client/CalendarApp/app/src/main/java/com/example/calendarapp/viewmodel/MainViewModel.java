package com.example.calendarapp.viewmodel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.calendarapp.data.Repository;
import com.example.calendarapp.model.Event;
import java.util.List;

//ensures data persistence

public class MainViewModel extends ViewModel {
    private final Repository repo = new Repository();
    
    public final MutableLiveData<List<Event>> events = new MutableLiveData<>();
    public final MutableLiveData<String> error = new MutableLiveData<>();
    public final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    public final MutableLiveData<Boolean> eventAdded = new MutableLiveData<>();
    public final MutableLiveData<Boolean> eventDeleted = new MutableLiveData<>();

    // loads calendar
    public void fetchMyCalendar(Integer userId) { 
        isLoading.setValue(true); 
        refreshCalendar(userId); 
    }

    // refreshes calendar
    public void refreshCalendar(Integer userId) {
        repo.getMyCalendar(userId, new Repository.RepositoryCallback<List<Event>>() {
            @Override public void onSuccess(List<Event> r) {isLoading.setValue(false);events.setValue(r);}
            @Override public void onError(String e) {isLoading.setValue(false);error.setValue(e);}
        });
    }

    // adds new event
    public void addEvent(Event ev) {
        isLoading.setValue(true);
        repo.addEvent(ev, new Repository.RepositoryCallback<Event>() {
            @Override public void onSuccess(Event r) {isLoading.setValue(false);eventAdded.setValue(true);}
            @Override public void onError(String e) {isLoading.setValue(false);error.setValue(e);}
        });
    }

    // deletes event by id
    public void deleteEvent(Integer id) {
        isLoading.setValue(true);
        repo.deleteEvent(id, new Repository.RepositoryCallback<Void>() {
            @Override public void onSuccess(Void r) {isLoading.setValue(false);eventDeleted.setValue(true);}
            @Override public void onError(String e) {isLoading.setValue(false);error.setValue(e);}
        });
    }

    // resets state indicators
    public void resetStatus() { 
        eventAdded.setValue(false); 
        eventDeleted.setValue(false); 
        error.setValue(null); 
    }
}
