import 'package:flutter/material.dart';
import 'package:travel_guide/features/bloc/list_of_countries_bloc.dart';
import 'package:travel_guide/features/models/countries_list.dart';
import 'package:travel_guide/features/screens/details_travel_info_screen.dart';

class TravelGuideScreen extends StatefulWidget {
  const TravelGuideScreen();

  @override
  State<StatefulWidget> createState() => TravelGuideScreenState();
}

class TravelGuideScreenState extends State<TravelGuideScreen> {
  static CountriesListDataBloc? countriesListDataBloc;
  int count = 0;

  @override
  void initState() {
    countriesListDataBloc ??= CountriesListDataBloc();
    countriesListDataBloc!.getCountriesListData();
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return StreamBuilder<List<CountriesList>?>(
      stream: countriesListDataBloc?.getCountriesListDataStream$,
      builder: (BuildContext context,
          AsyncSnapshot<List<CountriesList>?> countriesListSnapshot) {
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
          return buildListOfCounties(countriesListSnapshot.data!);
        }
      },
    );
  }

  Widget buildListOfCounties(List<CountriesList> snapShotData) {
    Widget onTapAction(String countryName) {
      return InkWell(
        child: Text(countryName),
        onTap: () {
          countriesListDataBloc!
              .getSpecificCountryData(countryName: countryName);
          Navigator.of(context).push(MaterialPageRoute<Widget>(
              builder: (BuildContext context) => DetailTravelInfoScreen(
                  countriesListDataBloc: countriesListDataBloc)));
        },
      );
    }

    return Scaffold(
        appBar: AppBar(
          title: Text('Travel Guide Links'),
        ),
        body: ListView.builder(
          // Let the ListView know how many items it needs to build.
          itemCount: snapShotData.length,
          // Provide a builder function. This is where the magic happens.
          // Convert each item into a widget based on the type of item it is.
          itemBuilder: (context, index) {
            final item = snapShotData[index];

            return Card(
                child: ListTile(
              title: onTapAction(item.name!),
              leading: Icon(Icons.label),
              trailing: Text("0${index + 1}"),
            ));
          },
        ));
  }

  @override
  void dispose() {
    countriesListDataBloc?.dispose();
    super.dispose();
  }
}
