import 'package:flutter/material.dart';
import 'package:travel_guide/features/bloc/list_of_countries_bloc.dart';
import 'package:travel_guide/features/models/county_detail.dart';

class DetailTravelInfoScreen extends StatelessWidget {
  const DetailTravelInfoScreen({this.countriesListDataBloc});
  final CountriesListDataBloc? countriesListDataBloc;

  @override
  Widget build(BuildContext context) {
    return StreamBuilder<CountyDetail?>(
      stream: countriesListDataBloc?.getCountrySpecificDataStream$,
      builder: (BuildContext context,
          AsyncSnapshot<CountyDetail?> countriesListSnapshot) {
        if (countriesListSnapshot.data == null ||
            countriesListSnapshot.hasError) {
          if (countriesListSnapshot.data == null) {
            return Container(
              color: Colors.white,
            );
          } else {
            return Text('Service error please try again later!');
          }
        } else {
          return buildCountyInfo(countriesListSnapshot.data!);
        }
      },
    );
  }

  Widget buildCountyInfo(CountyDetail snapShotData) {
    return Scaffold(
        appBar: AppBar(
          title: Text('Travel Guide Links'),
        ),
        body: buildNewPage(snapShotData));
  }

  Widget buildNewPage(CountyDetail snapShotData) {
    return Padding(
        padding: EdgeInsets.only(left: 10, right: 10, top: 20),
        child: Card(
            color: Colors.blueAccent.withOpacity(0.1),
            margin: EdgeInsets.only(left: 5, right: 5),
            child: Column(mainAxisSize: MainAxisSize.min, children: <Widget>[
              ListTile(
                leading: Icon(Icons.map, size: 50),
                title: Text(snapShotData.names!.continent!),
                subtitle: Text(snapShotData.names!.full!),
              ),
              buildRowTile(Text(
                'Latitude' + snapShotData.maps!.lat!,
                style: TextStyle(
                  fontSize: 15,
                  color: Colors.green[900],
                  fontWeight: FontWeight.w500,
                ), //T
                //Text
              )),
              SizedBox(
                height: 10,
              ),
              buildRowTile(
                Text('Longitude' + snapShotData.maps!.long!,
                    style: TextStyle(
                      fontSize: 15,
                      color: Colors.green[900],
                      fontWeight: FontWeight.w500,
                    )), //Textstyle
              ),
              SizedBox(
                height: 10,
              ),
              buildRowTile(Text(snapShotData.timezone!.name!)),
              SizedBox(
                height: 10,
              ),
              buildRowTile(Text('Ambulance phone number:  ' +
                  snapShotData.telephone!.ambulance!)),
              SizedBox(
                height: 10,
              ),
              buildRowTile(Text('Fire department phone number:  ' +
                  snapShotData.telephone!.fire!)),
              SizedBox(
                height: 10,
              ),
              buildRowTile(Text('Police department phone number:  ' +
                  snapShotData.telephone!.police!)),
              SizedBox(
                height: 10,
              ),
              buildRowTile(
                  Text('Currency Name:  ' + snapShotData.currency!.name!)),
              SizedBox(
                height: 10,
              ),
              buildRowTile(Text('Month min:  ' +
                  snapShotData.weather!.keys.first +
                  ' ' +
                  snapShotData.weather!.values.first.tMin!)),
              SizedBox(
                height: 10,
              ),
              buildRowTile(Text('Month max:  ' +
                  snapShotData.weather!.keys.first +
                  ' ' +
                  snapShotData.weather!.values.first.tMax!)),
              SizedBox(
                height: 10,
              ),
            ])));
  }
}

Widget buildRowTile(Widget childWidget) {
  return Padding(
      padding: EdgeInsets.only(left: 80),
      child: Row(children: [FittedBox(child: childWidget)]));
      child: Row(children)
}
