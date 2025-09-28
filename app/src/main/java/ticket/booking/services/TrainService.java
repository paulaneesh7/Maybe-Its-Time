package ticket.booking.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ticket.booking.entities.Train;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class TrainService {
    private List<Train> trainList;
    private ObjectMapper objectMapper = new ObjectMapper();
    private static final String TRAIN_DB_PATH = "../localDB/trains.json";

    public TrainService() throws IOException {
        File trains = new File(TRAIN_DB_PATH);
        trainList = objectMapper.readValue(trains, new TypeReference<List<Train>>() {});
    }

    public List<Train> searchTrains(String source, String destination) {
        return trainList.stream().filter(train -> {
            List<String> stationOrder = train.getStations();

            int sourceIndex = stationOrder.indexOf(source.toLowerCase());
            int destinationIndex = stationOrder.indexOf(destination.toLowerCase());

            return sourceIndex != -1 && destinationIndex != -1 && sourceIndex < destinationIndex;
        }).toList();
    }




}
