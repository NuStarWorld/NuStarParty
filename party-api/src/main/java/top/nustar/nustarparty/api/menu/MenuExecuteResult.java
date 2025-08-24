/*
 *    NuStarParty
 *    Copyright (C) 2025  NuStar
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package top.nustar.nustarparty.api.menu;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author NuStar
 * @since 2025/8/6 22:10
 */
@Data
@AllArgsConstructor
public class MenuExecuteResult {
    private boolean success;
    private String message;

    public static MenuExecuteResult success() {
        return success(null);
    }

    public static MenuExecuteResult success(String message) {
        return new MenuExecuteResult(true, message);
    }

    public static MenuExecuteResult failure(String message) {
        return new MenuExecuteResult(false, message);
    }

    public static MenuExecuteResult failure() {
        return failure(null);
    }
}
