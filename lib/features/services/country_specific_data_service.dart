import 'dart:convert';

import 'package:flutter_dotenv/flutter_dotenv.dart';
import 'package:http/http.dart';
import 'package:travel_guide/features/models/county_detail.dart';
import 'package:travel_guide/features/services/custom_http_exception.dart';

class CountriesSpecificDataService {
  factory CountriesSpecificDataService() {
    singleton ??= CountriesSpecificDataService._internal();
    client ??= Client();
    return singleton!;
  }

  CountriesSpecificDataService._internal();
  static CountriesSpecificDataService? singleton;
  static Client? client;
  Future<CountyDetail?> getCountriesSpecificDataServiceResponse(
      {String? countryName}) async {
    final dynamic _apiKey = dotenv.env['API_KEY'];
    try {
      final Map<String, String> headers = <String, String>{
        'TRAVEL_GUIDE_API_KEY': _apiKey
      };
      Response countriesDataResponse;

      countriesDataResponse = await client!.get(
          Uri.parse('https://travelbriefing.org/$countryName?format=json'),
          headers: headers);

      if (countriesDataResponse.statusCode == 200) {
        return CountyDetail.fromJson(json.decode(countriesDataResponse.body));
      }
    } catch (Error) {
      print('Error: $Error');
      throw InternalServerException(
          'Error occured while Communication with Server with StatusCode',
          uri:
              Uri(path: 'https://travelbriefing.org/$countryName?format=json'));
    }
  }
}
