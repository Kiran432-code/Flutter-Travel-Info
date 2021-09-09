import 'dart:convert';

import 'package:flutter_dotenv/flutter_dotenv.dart';
import 'package:http/http.dart';
import 'package:travel_guide/features/models/countries_list.dart';
import 'package:travel_guide/features/services/custom_http_exception.dart';

class CountriesListService {
  factory CountriesListService() {
    singleton ??= CountriesListService._internal();
    client ??= Client();
    return singleton!;
  }

  CountriesListService._internal();
  static CountriesListService? singleton;
  static Client? client;
  Future<List<CountriesList>?> getCountriesListServiceResponse() async {
    final dynamic _baseUrl = dotenv.env['API_URL'];
    final dynamic _apiKey = dotenv.env['API_KEY'];

    final Map<String, String> headers = <String, String>{
      'TRAVEL_GUIDE_API_KEY': _apiKey
    };
    Response countriesDataResponse;
    try {
      countriesDataResponse =
          await client!.get(Uri.parse(_baseUrl), headers: headers);

      if (countriesDataResponse.statusCode == 200) {
        return CountriesList()
            .fromJson(json.decode(countriesDataResponse.body));
      }
    } catch (Error) {
      print('Error: $Error');
      throw InternalServerException(
          'Error occurred while Communication with Server with StatusCode',
          uri: Uri(path: _baseUrl));
    }
  }
}
