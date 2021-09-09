import 'package:rxdart/rxdart.dart';
import 'package:travel_guide/features/models/countries_list.dart';
import 'package:travel_guide/features/models/county_detail.dart';
import 'package:travel_guide/features/repository/country_sepcific_data_repository.dart';
import 'package:travel_guide/features/repository/list_of_countries_repository.dart';
import 'package:travel_guide/features/services/list_of_countries_service.dart';

class CountriesListDataBloc {
  final BehaviorSubject<List<CountriesList>?> countriesListDataFetcher =
      BehaviorSubject<List<CountriesList>?>.seeded(null);

  ValueStream<List<CountriesList>?> get getCountriesListDataStream$ =>
      countriesListDataFetcher.stream;

  final BehaviorSubject<CountyDetail?> countrySpecificDataFetcher =
      BehaviorSubject<CountyDetail?>.seeded(null);

  ValueStream<CountyDetail?> get getCountrySpecificDataStream$ =>
      countrySpecificDataFetcher.stream;

  Future<void> getCountriesListData({CountriesListService? service}) async {
    try {
      final List<CountriesList>? countriesResponseList =
          await CountriesListRepository()
              .getCountriesResponseList(service: service);

      if (!countriesListDataFetcher.isClosed) {
        countriesListDataFetcher.add(countriesResponseList);
      }
    } catch (e) {
      if (!countriesListDataFetcher.isClosed) {
        countriesListDataFetcher.addError(e);
      }
    }
  }

  Future<void> getSpecificCountryData({String? countryName}) async {
    try {
      final CountyDetail? countrySpecificResponseList =
          await CountriesSpecificDataRepository()
              .getCountrySpecificResponse(countryName: countryName);

      if (!countrySpecificDataFetcher.isClosed) {
        countrySpecificDataFetcher.add(countrySpecificResponseList);
      }
    } catch (e) {
      if (!countrySpecificDataFetcher.isClosed) {
        countrySpecificDataFetcher.addError(e);
      }
    }
  }

  void dispose() {
    countriesListDataFetcher.close();
    countrySpecificDataFetcher.close();
  }
}
